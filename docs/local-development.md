# ローカル開発

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
