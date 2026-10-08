# kokorono_note インフラ仕様書

## 1. 概要

本アプリケーションでは、ローカル開発環境と本番環境を明確に分離する。

本番環境は以下の構成とする。

```text
Frontend
  └─ Vercel

Backend
  └─ Render
      └─ Spring Boot

Database
  └─ Supabase
      └─ PostgreSQL
```

ローカル環境では、本番DBを使用せず、Docker上にPostgreSQLを構築する。

```text
Local Frontend
  ↓
Local Spring Boot
  ↓
Docker PostgreSQL
```

---

## 2. Frontend

Vercelを使用する。

### 役割

- フロントエンドアプリケーションのホスティング
- 本番フロントエンドの配信
- フロントエンド用環境変数の管理

フロントエンドからDBへ直接アクセスしない。

必ずSpring Boot APIを経由する。

```text
Frontend
  ↓
Spring Boot API
  ↓
PostgreSQL
```

---

## 3. Backend

Renderを使用する。

Render上でSpring Bootアプリケーションを稼働させる。

### 使用技術

```text
Java 21
Spring Boot 4.1.1
Spring MVC
Spring Security
Spring Security OAuth2 Client
Spring Data JPA
Validation
```

Renderはアプリケーション実行環境として使用する。

DBはRender上には配置しない。

```text
Render
  └─ Spring Boot
        ↓
     Supabase
```

---

## 4. Production Database

Supabase PostgreSQLを使用する。

Supabaseは今回、PostgreSQLのマネージドホスティングサービスとして利用する。

Supabase固有機能への依存は最小限にする。

### 使用しないもの

```text
Supabase Auth
Supabaseからフロントエンドへの直接DBアクセス
```

認証・認可はSpring Boot側で管理する。

---

## 5. Local Database

ローカル開発ではDocker上のPostgreSQLを使用する。

```text
Developer PC
  │
  ├─ Spring Boot
  │
  └─ Docker
       └─ PostgreSQL
```

本番Supabase DBをローカル開発から通常利用しない。

### 目的

- 本番データとの分離
- 開発中にDBを自由に変更できる
- DB破棄・再作成が容易
- Migrationを安全に検証できる

---

## 6. 環境分離

Spring Boot Profileを使用して環境を分離する。

想定構成：

```text
src/main/resources/

application.yml
application-local.yml
application-prod.yml
```

### local

```text
Spring Boot
  ↓
localhost
  ↓
Docker PostgreSQL
```

### prod

```text
Render Spring Boot
  ↓
Supabase PostgreSQL
```

---

## 7. DB接続情報

DBのユーザー名・パスワード・接続URLなどの秘密情報はGitリポジトリへ保存しない。

本番ではRenderの環境変数から取得する。

想定：

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Spring Boot側：

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

ローカル環境については、Docker Compose側の設定と対応させる。

---

## 8. DB Migration

DBスキーマ管理にはFlywayを使用する。

JPA/Hibernateの自動スキーマ更新だけに依存しない。

Migrationファイル例：

```text
src/main/resources/db/migration/

V1__create_users.sql
V2__create_workspaces.sql
V3__create_workspace_members.sql
```

同一Migrationを以下の両方へ適用する。

```text
Local PostgreSQL
Production Supabase PostgreSQL
```

これにより、ローカルと本番のDB構造の差異を防ぐ。

---

## 9. JPA

DBアクセスにはSpring Data JPAを使用する。

主なEntityはアプリケーション仕様に従って定義する。

本番ではHibernateによる意図しないDB変更を避ける。

FlywayをDB構造管理の主体とする。

---

## 10. Google OAuth

認証にはGoogle OAuthを使用する。

Google OAuthはGoogleによる本人確認のために使用する。

Google APIの継続利用を目的としない。

### 保存しないもの

```text
Google Refresh Token
Gmailデータ
Google API用Access Tokenの永続保存
```

ログイン後はアプリケーション側でユーザーを管理する。

---

## 11. Google OAuth環境分離

Google OAuthではローカル環境と本番環境それぞれのRedirect URIを登録する。

例：

```text
Local
http://localhost:xxxx/...

Production
https://本番Backendドメイン/...
```

Google OAuthのClient Secret等もGitには保存しない。

本番ではRenderの環境変数として管理する。

### JWT署名用RSA鍵

JWTの署名・検証には、同じRSA鍵ペアの秘密鍵と公開鍵を使う。
`JWT_PRIVATE_KEY`にはPKCS#8 DERのBase64、`JWT_PUBLIC_KEY`にはX.509 DERのBase64を設定する。
値にはPEMヘッダー、引用符、改行を含めない。

ローカルではプロジェクトルートの`.env`に設定し、`local`プロファイルを有効にして起動する。
既存の`application-local.yml`の`spring.config.import`が`.env`を読み込む。
`.env`はJava properties形式のため、`export`や値を囲む引用符は付けない。

```properties
JWT_PRIVATE_KEY=秘密鍵のBase64値
JWT_PUBLIC_KEY=公開鍵のBase64値
```

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

鍵を生成する場合は、Git管理外の作業ディレクトリで以下を実行する。
`private-key.pem`は秘密鍵なので、リポジトリには保存しない。

```sh
umask 077
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private-key.pem
openssl pkcs8 -topk8 -nocrypt -in private-key.pem -outform DER | openssl base64 -A
openssl pkey -in private-key.pem -pubout -outform DER | openssl base64 -A
```

`openssl pkcs8`の出力を`JWT_PRIVATE_KEY`、`openssl pkey`の出力を`JWT_PUBLIC_KEY`に設定する。

本番ではRenderの対象サービスのEnvironmentに同じ2変数を登録し、再デプロイする。
本番用にはローカルとは別の鍵ペアを用意し、`local`プロファイルは有効にしない。
鍵は起動時に読み込み、未設定・不正な形式・鍵の不一致の場合は起動を失敗させる。
再起動のたびに鍵を生成する処理はない。鍵を変更すると変更前のJWTは検証できなくなる。

---

## 12. セキュリティ方針

秘密情報はGitHubへコミットしない。

対象：

```text
DB_PASSWORD
DB_USERNAME
DB_URL
Google Client Secret
その他API Secret
```

必要に応じて `.env` 等を利用する場合も、必ず `.gitignore` の対象とする。

---

## 13. 通信経路

本番環境の基本的な通信経路は以下とする。

```text
User
  │
  │ HTTPS
  ▼
Vercel
Frontend
  │
  │ HTTPS API Request
  ▼
Render
Spring Boot
  │
  │ PostgreSQL Connection
  ▼
Supabase
PostgreSQL
```

フロントエンドからSupabaseへの直接接続は行わない。

---

## 14. 環境構成

### Local

```text
Frontend
  └─ localhost

Backend
  └─ localhost
      └─ Spring Boot

Database
  └─ Docker PostgreSQL
```

### Production

```text
Frontend
  └─ Vercel

Backend
  └─ Render
      └─ Spring Boot

Database
  └─ Supabase
      └─ PostgreSQL
```

---

## 15. デプロイ単位

各サービスを独立してデプロイする。

```text
Frontend Project
  ↓
Vercel

Backend Project
  ↓
Render

Database
  ↓
Supabase
```

Spring BootからSupabaseへ接続する。

---

## 16. 現時点で採用しないもの

MVP時点では以下は使用しない。

```text
AWS
Render PostgreSQL
Supabase Auth
FrontendからのDB直接アクセス
Google Refresh Tokenの永続保存
Gmailデータの保存
過剰なインフラ構成
```

---

## 17. インフラ全体像

```text
                     Production

                        User
                          │
                          ▼
                    ┌─────────┐
                    │ Vercel  │
                    │Frontend │
                    └────┬────┘
                         │ HTTPS
                         ▼
                    ┌─────────┐
                    │ Render  │
                    │Spring   │
                    │ Boot    │
                    └────┬────┘
                         │
                         │ PostgreSQL
                         ▼
                    ┌──────────┐
                    │ Supabase │
                    │PostgreSQL│
                    └──────────┘


                      Local

                   Developer PC

                    ┌─────────┐
                    │Frontend │
                    └────┬────┘
                         │
                         ▼
                    ┌─────────┐
                    │Spring   │
                    │ Boot    │
                    └────┬────┘
                         │
                         ▼
                    ┌─────────┐
                    │ Docker  │
                    │Postgres │
                    └─────────┘
```

---

## 18. 現時点の確定方針

```text
Frontend
→ Vercel

Backend
→ Render
→ Spring Boot 4.1.1
→ Java 21

Production DB
→ Supabase PostgreSQL

Local DB
→ Docker PostgreSQL

DB Migration
→ Flyway

ORM
→ Spring Data JPA

Authentication
→ Google OAuth
→ Spring Security

Secrets
→ 環境変数管理
→ GitHubへ保存しない

Production DBへのアクセス
→ Spring Bootのみ

Supabase Auth
→ 使用しない
```
