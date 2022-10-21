package com.example.shuffletool.service;

import java.nio.file.Path;

import com.example.shuffletool.entity.Config;

// デッキリストと設定値のファイル読み書きを担当するサービスクラス
public interface FileService {
	// デッキリストのファイルを読み込み、エンティティに保持するメソッド
	public void loadDeck(Path path);

	// 入力されたデッキリストを読み込み、ファイルに書き込んでエンティティに保持するメソッド
	public void writeDeck(String deck, Path path);

	// デッキリストをtextareaの初期値として表示できるようにStringに変換
	public String deckToString();

	// 設定値のファイルを読み込み、エンティティに保持
	public void loadConfig(Path path);

	// 入力された設定値を読み込み、ファイルに書き込んでエンティティに保持するメソッド
	public void writeConfig(Path path, Config config);

}
