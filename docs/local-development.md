# ローカル開発

## 実装方針

本アプリは収益化を目的としないため、実装はシンプルかつ必要最低限とする。
現在必要な機能を、読みやすく保守しやすいコードで実現することを優先する。
将来の拡張や大規模運用を見越した抽象化、共通化、依存ライブラリ、インフラ構成は、実際に必要になってから追加する。
認証・認可や入力検証など、データを守り正しく動作させるために必要な処理は維持する。
テストも重要な動作の確認に絞り、実装と運用の負担を抑える。

## 起動とテスト

プロファイルを指定しない場合は `local` で起動します。

1. `.env` に `POSTGRES_PASSWORD`、`JWT_PRIVATE_KEY`、`JWT_PUBLIC_KEY`、`GOOGLE_CLIENT_ID`、`GOOGLE_CLIENT_SECRET` を設定します。JWT鍵の形式は `.env.example` を参照してください。
2. `docker compose up -d postgres` でDBを起動します。
3. `./mvnw spring-boot:run` でアプリを起動します。

認証設定は1つの `SecurityConfig` に統一し、`local` と `prod` の両方でGoogle OAuthとJWT認証を使います。`/auth/token` と `/auth/refresh`、Googleログイン用のURL以外は認証が必要です。`/auth/token` はCSRF検証の対象外ですが、Cookieを使う `/auth/refresh` はCSRFトークンが必要です。

Google側にリダイレクトURI `http://localhost:8080/login/oauth2/code/google` を登録し、フロントの `http://localhost:3000/login` からログインを開始します。フロントが `/auth/login` 経由でランダムな `flow` とHttpOnly Cookieを作成し、APIの `/oauth2/authorization/google?flow=...` へ遷移します。APIはOAuthセッションにflowを保持し、成功後に一時コードとともにフロントへ返します。APIのGoogleログインURLを直接開く方法は使いません。ログイン成功後は `http://localhost:3000/oauth/callback` に一時コードを付けて戻ります。

テストはDBを起動してから `./mvnw test` で実行します。

## 本番環境

`SPRING_PROFILES_ACTIVE=prod` を指定して起動します。環境差分は `application-local.yml` と `application-prod.yml` で管理し、本番では `.env` を読み込まず環境変数を使います。

次の環境変数を設定してください。

- `DATABASE_URL`（`jdbc:postgresql://...`）、`DATABASE_USERNAME`、`DATABASE_PASSWORD`
- `JWT_PRIVATE_KEY`、`JWT_PUBLIC_KEY`（形式は `.env.example` を参照）
- `GOOGLE_CLIENT_ID`、`GOOGLE_CLIENT_SECRET`
- `FRONTEND_URL`（フロントエンドのURL）

Google側には本番APIの `/login/oauth2/code/google` のURLも登録してください。

### Render の Docker Web Service

バックエンドのリポジトリ直下をビルドコンテキストにし、Render で Web Service の Language に **Docker** を選択します。ルートの `Dockerfile` を使用し、追加の Build Command / Start Command は設定しません。Dockerfile は Maven と Java 21 で JAR を作成し、Java 21 JRE の実行用イメージに JAR のみをコピーします。ビルド時のテストは省き、公開前にローカルDBで `./mvnw test` を実行します。

| Render の環境変数 | 内容 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod`。必須。ローカル用 `.env` 読み込みを避ける。 |
| `DATABASE_URL` | Supabase の `jdbc:postgresql://...` URL。TLS の設定を含める。 |
| `DATABASE_USERNAME`、`DATABASE_PASSWORD` | 上記接続先の認証情報。 |
| `JWT_PRIVATE_KEY`、`JWT_PUBLIC_KEY` | 本番用の同じ RSA 鍵ペア。形式は `.env.example` を参照。 |
| `GOOGLE_CLIENT_ID`、`GOOGLE_CLIENT_SECRET` | Google OAuth Web クライアントの認証情報。 |
| `FRONTEND_URL` | Vercel の正式な HTTPS オリジン。末尾に `/` を付けない。 |
| `PORT` | Render が指定。`application-prod.yml` の `server.port` が読み取り、未指定時は `8080`。 |

`PORT` は通常 Render が提供するため、独自に固定する必要はありません。Spring Boot はコンテナ内の `0.0.0.0` で待ち受けます。Render の HTTPS 終端後に届く転送ヘッダーを本番プロファイルで解釈し、Google OAuth の戻り先を HTTPS で生成します。Google Cloud には `https://<APIドメイン>/login/oauth2/code/google` を承認済みリダイレクトURIとして登録してください。`FRONTEND_URL` はこの Google 向けURIではなく、認証後のフロントへの戻り先です。

Render から Supabase の通常の直接接続先は IPv6 制約で到達できない場合があります。接続情報は Supabase の Connect 画面から IPv4 対応の **session pooler** を選び、JDBC・Flyway の起動時接続と TLS を確認してください。Flyway は既存のマイグレーションをアプリ起動時に適用し、JPA は本番で `ddl-auto: validate` のままです。初回接続前に対象DBのバックアップ／復元手段を確認してください。

ローカルでイメージだけを検証する場合は、バックエンドのリポジトリ直下で `docker build -t kokorono-note-api:local .` を実行します。`.dockerignore` が `.env` や鍵ファイルを除外し、ビルドには秘密値を渡しません。本番の秘密値は Render の Environment に設定し、Dockerfile やログへ記載しないでください。

## フロント経由の認証

フロントはAccess Token、Refresh Token、CSRF用JSESSIONIDを暗号化したHttpOnly Cookieで保持し、APIへの通信をサーバー経由で行う。
`GET /auth/csrf` のトークンと対応するセッションCookieを取得し、refreshを含む更新系APIへ両方を送る。
APIのRefresh Token CookieはlocalではSecure=false、prodではSecure=trueとする。フロント側のCookieはフロント自身の環境に合わせて設定する。
Cookieやトークンの値はログへ出力しない。
