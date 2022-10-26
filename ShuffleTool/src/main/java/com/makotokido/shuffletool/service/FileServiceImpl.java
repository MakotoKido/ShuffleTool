package com.makotokido.shuffletool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.makotokido.shuffletool.entity.Config;
import com.makotokido.shuffletool.entity.DeckList;

// // デッキリストと設定値のファイル読み書きを担当するサービスクラス
@Service
public class FileServiceImpl implements FileService {
	/*
	 * デッキリスト
	 */
	@Autowired
	private DeckList decklist;

	// デッキリストのファイルを読み込み、エンティティに保持
	@Override
	public void loadDeck(Path path) {
		// デッキリストをファイルから読み込み
		List<String> list = loadFile(path);
		// それぞれのカードに番号を振る
		for (int id = 0; id < list.size(); id++) {
			// 1からリストの順番に番号を名前の前にくっつける
			// スペースの数は桁に応じて変える(2桁枚までを想定)
			String sp;
			if (id < 9) {
				sp = "&nbsp;&nbsp;&nbsp;";
			} else {
				sp = "&nbsp;";
			}
			list.set(id, (id + 1) + sp + list.get(id));
		}

		// 読み込んだデッキリストをエンティティに保持
		decklist.setOriginal(list);
	}

	// 入力されたデッキリストをファイルに書き込む
	@Override
	public void writeDeck(String deck, Path path) {
		// ファイルを書き込めるよう、listに格納
		List<String> line = new ArrayList<String>();
		line.add(deck);
		// 書き込み(上書き)
		try {
			Files.write(path, line, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ファイルから読み込んだデッキリストをtextareaの初期値として表示できるようにStringに変換
	@Override
	public String deckToString(Path path) {
		// builderに読み込み結果を格納
		StringBuilder builder = new StringBuilder();
		// デッキリストをファイルから取得
		List<String> list = loadFile(path);

		// listの要素1つずつに改行文字を挟んで1つの文字列にする
		for (String s : list) {
			builder.append(s + "&#13;");
		}
		// 最後の改行文字は不要なので削除
		builder.delete(builder.length() - 5, builder.length());

		return builder.toString();
	}

	/*
	 * 設定値
	 */
	@Autowired
	private Config config;

	// 各設定項目のデフォルト値、項目名
	// ディールシャッフル
	// 作る山の個数
	private final int DEALSTACKS = 7;
	private final String DSTACK = "dealstacks";
	// 山に配るカードの枚数がブレる確率(単位:%)
	private final int DEALFLUC = 5;
	private final String DFLUC = "dealfluc";

	// ファローシャッフル
	// 山を組み合わせる際に、間に挟まるカードが2,3枚のいずれかになる確率(単位:%)
	private int FAROFLUC = 20;
	private final String FFLUC = "farofluc";

	// ヒンズーシャッフル・ファローシャッフル共通
	// 分ける山の枚数がちょうど半分からブレる最大枚数のデッキに対する割合(単位:%)
	private final int SPLITFLUC = 13;
	private final String SFLUC = "splitfluc";

	// 設定値のファイルを読み込み、エンティティに保持
	@Override
	public void loadConfig(Path path) {
		// 設定ファイルを読み込む
		List<String> confs = loadFile(path);

		// 設定値の存在フラグ
		boolean dstackExists = false;
		boolean dflucExists = false;
		boolean sflucExists = false;
		boolean fflucExists = false;
		// 読み込んだファイルの項目を判別し、それぞれエンティティに設定していく
		for (String line : confs) {
			// 項目名と設定値を=で切り離す
			String[] arr = line.split("=");
			String attrib = arr[0]; // 項目名
			try {
				int val = Integer.parseInt(arr[1]); // 設定値をint型に変換
				// 変換成功時のみエンティティに設定
				switch (attrib) {
				case DSTACK:
					config.setDealStacks(val);
					dstackExists = true;
					break;
				case DFLUC:
					config.setDealFluc(val);
					dflucExists = true;
					break;
				case SFLUC:
					config.setSplitFluc(val);
					sflucExists = true;
					break;
				case FFLUC:
					config.setFaroFluc(val);
					fflucExists = true;
					break;
				default:
					// 何もしない
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		// 読み込んだファイルに設定値が存在しない場合、デフォルト値をエンティティに設定
		if (!dstackExists) {
			config.setDealStacks(DEALSTACKS);
		}
		if (!dflucExists) {
			config.setDealFluc(DEALFLUC);
		}
		if (!sflucExists) {
			config.setSplitFluc(SPLITFLUC);
		}
		if (!fflucExists) {
			config.setFaroFluc(FAROFLUC);
		}
	}

	// 設定値をファイルに書き込む
	@Override
	public void writeConfig(Path path, Config config) {
		// 書き込めるように入力値を作成してlinesに格納
		List<String> lines = new ArrayList<String>();
		lines.add(DSTACK + "=" + config.getDealStacks());
		lines.add(DFLUC + "=" + config.getDealFluc());
		lines.add(FFLUC + "=" + config.getFaroFluc());
		lines.add(SFLUC + "=" + config.getSplitFluc());

		// 書き込み(上書き)
		try {
			Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 設定値をデフォルトに戻す
	@Override
	public void setDefault(Path path) {
		// 設定値のファイルが空白の場合、設定値がデフォルトになることを利用
		// 設定値のファイルを空白に設定
		try {
			Files.write(path, new ArrayList<String>(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// デフォルトの設定値を読み込み
		loadConfig(path);
		// 設定値をファイルに書き込み
		writeConfig(path, config);
		
	}

	/*
	 * ユーティリティメソッド
	 */
	// 与えられたパスのテキストファイルを1行ごとにList<String>の要素にして返す
	private List<String> loadFile(Path path) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

}
