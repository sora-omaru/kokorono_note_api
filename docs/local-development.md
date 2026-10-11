# ローカル開発

## 実装方針

本アプリは収益化を目的としないため、実装はシンプルかつ必要最低限とする。
現在必要な機能を、読みやすく保守しやすいコードで実現することを優先する。
将来の拡張や大規模運用を見越した抽象化、共通化、依存ライブラリ、インフラ構成は、実際に必要になってから追加する。
認証・認可や入力検証など、データを守り正しく動作させるために必要な処理は維持する。
テストも重要な動作の確認に絞り、実装と運用の負担を抑える。

## 起動とテスト

プロファイルを指定しない場合は `local` で起動します。

1. `.env` に `POSTGRES_PASSWORD`、`JWT_PRIVATE_KEY`、`JWT_PUBLIC_KEY` を設定します。JWT鍵の形式は `.env.example` を参照してください。
2. `docker compose up -d postgres` でDBを起動します。
3. `./mvnw spring-boot:run` でアプリを起動します。

`local` ではGoogle OAuthを無効にし、トークンなしのAPIリクエストを許可します。Bearerトークンを送った場合はJWTを検証します。JWTを使うサービスも同じ `.env` の鍵を使用します。ログイン成功による一時コードの発行は行いません。

テストはDBを起動してから `./mvnw test` で実行します。

## Google認証を有効にする場合

`local` 以外のプロファイルを明示すると既存の `SecurityConfig` が有効になります。その環境にDB接続設定、JWT鍵、`app.frontend-url` と次のGoogleクライアント登録を設定してください。

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

Google側にもリダイレクトURI（ローカルなら `http://localhost:8080/login/oauth2/code/google`）を登録します。`local` の認証不要設定はローカル開発用です。
