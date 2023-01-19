# コーディング規約

## 全体

基本的には[Kotlin 標準](https://kotlinlang.org/docs/coding-conventions.html)に従いますが、 この文書では追加の規約を定めます。

## 型名の命名規則

* パッケージ名が複数の単語から成る場合は、必ずcamelCaseで書いてください。
* コマンドの型名は、必ず `Command` から始めてください。
* モジュールの型名は、必ず `Module` で終わらせてください。
* イベントハンドラーの型名は、必ず `Handler` で終わらせてください。
* X Phoneアプリの型名は、必ず `App` で終わらせてください。
* フックの型名は、必ず `Hook` で終わらせてください。
* イベントの型名は必ず `Event` で終わらせてください。
* 基底クラス（スーパークラス）の型名は、必ず `Base` で終わらせてください。

## メンバーの命名規則

* Player Storeのキーを表す定数は、「PS_KEY_」からはじめて、大文字のスネークケースで表現してください。

## 制御構文

### 1行でもブロック記法に

1行で終わるifステートメントであっても、基本的に `{}` を挿入してください。後で処理を追加する可能性があるためです。

if以外でも同様です。

ただし、アーリーリターンですぐにreturnなどを返す場合はこの限りではありません。

**○ OK**

```kotlin
if (player.name == "_knit_") {
    player.banPlayer()
}
```

**× NG**

```kotlin
if (player.name == "_knit_") player.banPlayer()
```

## if ステートメント

### アーリーリターンを心掛けて

if ステートメントをアーリーリターン（すぐにreturnを返す）に置き換えられる場合は、なるべくそのように記述してください。

**○ OK**

```kotlin
if (!isCitizen) return

openSpecialMenu()
```

**× NG**

```kotlin
if (isCitizen) {
    openSpecialMenu()
}
```