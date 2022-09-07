# MyCreatures作成手順

まずは植物版を作成する

アプリ名：PlantpediA

## database保存先

(debug build 限定)/data/data/com.websarva.wings.android.mycreatures


## MY植物図鑑

### 必要なレイアウト
　・ホーム画面
　・個別画面
　・木遷移画面

### 木遷移画面（PhylogeneticTree.kt)
個別画面と属画面を判別して表示する
共通で良いか？

#### 共通部品

##### 親アイテム
上に表示する
ボタンでとべる

##### 説明欄
編集可能

#### 種表示画面（activity_phylogenetic_edge.xml）


#### 属表示画面（activity_phylogenetic_tree.xml）

下にある植物の写真をまとめて表示する

#### 小アイテム（リスト）
ListViewを用いて表示する


　・説明　　

　・個別表示
　　異なるレイアウトファイルを使用する


#### 追加機能

#### 編集機能

説明欄などを編集できる




#### 保存機能
　・本　第10章
　・ファイル形式を決める　-> json?
　　必要な関数
　　・treeToJson
　　・jsonToTree


### ツリー表示
　リスト表示とは違う表示機能
　完全に別のレイアウトファイルが必要？
　切り替えして表示する？


## デザイン
　・マテリアルデザイン
　　・本　第16章
　・

## 最近の履歴機能

## クリアチェッカー






