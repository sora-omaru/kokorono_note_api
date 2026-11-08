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

## フロント経由の認証

フロントはAccess Token、Refresh Token、CSRF用JSESSIONIDを暗号化したHttpOnly Cookieで保持し、APIへの通信をサーバー経由で行う。
`GET /auth/csrf` のトークンと対応するセッションCookieを取得し、refreshを含む更新系APIへ両方を送る。
APIのRefresh Token CookieはlocalではSecure=false、prodではSecure=trueとする。フロント側のCookieはフロント自身の環境に合わせて設定する。
Cookieやトークンの値はログへ出力しない。
