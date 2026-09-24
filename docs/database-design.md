# 心のノート V1 仕様書

## 1. アプリ概要

「心のノート」は、旅行・引っ越し・イベントなどの出来事について、注意点・忘れ物・持ち物・次回へのメモなどを整理し、複数人で共有・編集できるアプリ。

データは以下の階層で管理する。

```text
Workspace（大大項目）
└─ Event（大項目）
   └─ Category（中項目）
      └─ Note（小項目）
```

例：

```text
家族の心のノート
└─ 沖縄旅行
   ├─ 忘れ物
   │  ├─ モバイルバッテリー
   │  └─ 日焼け止め
   └─ 注意点
      └─ レンタカーは早めに予約
```

---

## 2. Workspace

Workspaceは複数の出来事をまとめる共有単位。

- 1人でも利用可能
- 複数人でも利用可能
- 複数Accountを紐付け可能
- 参加者は全員編集可能
- 管理者・閲覧専用などの権限分けはV1では行わない
- Workspace作成者も通常の参加者として `workspace_account` に登録する


---

## 3. Event

Eventは具体的な出来事を表す。

例：

```text
沖縄旅行
引っ越し
ディズニー旅行
```

1つのWorkspaceに複数Eventを登録できる。

---

## 4. Category

CategoryはEvent内の分類を表す。

例：

```text
忘れ物
注意点
持ち物
次回やること
```

1つのEventに複数Categoryを登録できる。

---

## 5. Note

NoteはCategoryに属する具体的な内容。

例：

```text
モバイルバッテリーを忘れない
空港には2時間前に到着する
```

V1では以下の仕様とする。

- 内容は文章のみ
- チェック済み/未チェックは持たない
- タグは持たない
- 画像は持たない
- 手動並び替えは行わない
- 登録順で表示する

基本的に、

```sql
ORDER BY created_at ASC
```

で取得する。

---

## 6. Account・認証

認証方式はGoogleログインのみ。

AccountはGoogleアカウントを元に管理する。

想定項目：

```text
account
- id
- google_sub
- email
- display_name
- created_at
- updated_at
```

ユーザー識別はメールアドレスではなく、Googleの `sub` を基準とする。

---

## 7. JWT認証

Googleログイン後、アプリ独自のJWTを発行する。

基本フロー：

```text
Googleログイン
↓
Googleユーザー情報をバックエンドで確認
↓
Account取得または作成
↓
Access Token発行
↓
APIアクセス時にJWTを送信
```

APIアクセス時は、

```text
Authorization: Bearer <access-token>
```

を使用する。

バックエンドでは、

```text
JWTが有効か
↓
どのAccountか
↓
そのAccountが対象Workspaceに所属しているか
↓
所属していれば処理許可
```

という流れで認可する。

---

## 8. Refresh Token

Refresh Tokenも導入する想定。

役割：

```text
Access Token
→ API利用用
→ 短い有効期限

Refresh Token
→ Access Token再発行用
→ 長めの有効期限
```

DBではRefresh Tokenを管理する。

想定構成：

```text
refresh_token
- id
- account_id
- token_hash
- expires_at
- created_at
- revoked_at
```

生のRefresh Tokenそのものではなく、ハッシュ値を保存する想定。

---

## 9. WorkspaceとAccountの関係

AccountとWorkspaceは多対多。

そのため中間テーブルを持つ。

```text
workspace_account
- workspace_id
- account_id
- joined_at
```

同じAccountが同じWorkspaceへ二重参加しないようにする。

例えば、

```sql
PRIMARY KEY (workspace_id, account_id)
```

または同等のUNIQUE制約を設定する。

---

## 10. 招待仕様

Workspaceへの参加には招待コードを使用する。

Workspace自体に固定コードを持たせ続けるのではなく、必要なときだけ招待コードを発行する。

想定テーブル：

```text
workspace_invite
- id
- workspace_id
- code
- expires_at
- created_at
```

仕様：

- ランダムな招待コードを発行
- 有効期限は30分
- 30分以内なら複数人が利用可能
- 期限切れ後は利用不可
- 招待コードは再発行可能
- 1 Workspaceにつき、有効な招待コードは1つだけ

再発行時:
1. 既存の workspace_invite を削除
2. 新しい6桁コードを生成
3. expires_at を30分後に設定
4. 新しい workspace_invite を作成

参加フロー：

```text
Googleログイン
↓
招待コード入力
↓
招待コードを検索
↓
有効期限を確認
↓
対象Workspaceを取得
↓
workspace_accountへ登録
```

---

## 11. 権限

Workspaceに所属するAccountは全員同じ権限を持つ。

参加者は以下を実行可能。

```text
Eventの作成・編集・削除
Categoryの作成・編集・削除
Noteの作成・編集・削除
```

V1では以下は実装しない。

```text
管理者
オーナー専用操作
閲覧専用
細かいロール管理
```

---

## 12. 削除仕様

親を削除した場合、その配下はすべて削除する。

```text
Workspace削除
↓
WorkspaceAccount
WorkspaceInvite
Event
Category
Note
すべて削除
```

同様に、

```text
Event削除
→ 配下のCategoryとNoteを削除

Category削除
→ 配下のNoteを削除
```

DBでは基本的に外部キーの `ON DELETE CASCADE` を使用する。

ただしAccount削除については別途設計する。

---

## 13. DB構成

V1では以下の8テーブルを想定する。

```text
account
refresh_token
workspace
workspace_account
workspace_invite
event
category
note
```

全体構成：

```text
account
  │
  ├── refresh_token
  │
  └── workspace_account
         │
         └── workspace
                ├── workspace_invite
                └── event
                      └── category
                            └── note
```

リレーション：

```text
Account 1 ─── N RefreshToken

Account N ─── N Workspace
        \     /
     WorkspaceAccount

Workspace 1 ─── N WorkspaceInvite

Workspace 1 ─── N Event

Event 1 ─── N Category

Category 1 ─── N Note
```

---

## 14. V1では実装しない機能

初期バージョンでは以下は対象外。

```text
タグ
検索
画像添付
通知
閲覧専用ユーザー
管理者権限
ドラッグ&ドロップ並び替え
Noteのチェック済み/未チェック
細かい権限管理
```

---

## 15. V1の完成イメージ

最初の完成目標は以下。

```text
Googleログイン
↓
Workspace作成
↓
Event作成
↓
Category作成
↓
Note作成
↓
招待コード発行
↓
別ユーザーが30分以内に参加
```
