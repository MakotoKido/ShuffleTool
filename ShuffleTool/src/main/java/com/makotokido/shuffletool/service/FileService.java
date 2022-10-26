package com.makotokido.shuffletool.service;

import java.nio.file.Path;

import com.makotokido.shuffletool.entity.Config;

// デッキリストと設定値のファイル読み書きを担当するサービスクラス
public interface FileService {
	// デッキリストのファイルを読み込み、エンティティに保持
	public void loadDeck(Path path);

	// 入力されたデッキリストをファイルに書き込む
	public void writeDeck(String deck, Path path);

	// ファイルから読み込んだデッキリストをtextareaの初期値として表示できるようにStringに変換
	public String deckToString(Path path);

	// 設定値のファイルを読み込み、エンティティに保持
	public void loadConfig(Path path);

	// 入力された設定値をファイルに書き込む
	public void writeConfig(Path path, Config config);

	// 設定値をデフォルトに戻す
	public void setDefault(Path path);
}
