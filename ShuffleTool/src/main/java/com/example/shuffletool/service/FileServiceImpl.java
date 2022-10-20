package com.example.shuffletool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.shuffletool.entity.Config;
import com.example.shuffletool.entity.DeckList;

// ファイルの読み書きを行うサービスクラス
@Service
public class FileServiceImpl implements FileService {
	/*
	 * デッキリスト
	 */
	@Autowired
	private DeckList decklist;

	@Override
	public void loadDeck(Path path) {
		// デッキリストをファイルから読み込み
		List<String> list = loadFile(path);

		// 読み込んだデッキリストをエンティティに保持
		decklist.setOriginal(list);
	}

	@Override
	public void writeDeck(String deck, Path path) {
		// TODO Auto-generated method stub

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

	// ヒンズーシャッフル・ファローシャッフル共通
	// 分ける山の枚数が半分からブレる枚数のデッキに対する割合(単位:%)
	private final int SPLITFLUC = 13;
	private final String SFLUC = "splitfluc";

	// ファローシャッフル
	// 山を組み合わせる際に、間に挟まるカードが0,2,3枚のいずれかになる確率(単位:%)
	private int FAROFLUC = 20;
	private final String FFLUC = "farofluc";

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
			// TODO:変換時のエラー回避
			int val = Integer.parseInt(arr[1]); // 設定値をint型に変換

			// TODO:ここswitchでよくね
			if (DSTACK.equals(attrib)) {
				config.setDealStacks(val);
				dstackExists = true;
			} else if (DFLUC.equals(attrib)) {
				config.setDealFluc(val);
				dflucExists = true;
			} else if (SFLUC.equals(attrib)) {
				config.setSplitFluc(val);
				sflucExists = true;
			} else if (FFLUC.equals(attrib)) {
				config.setFaroFluc(val);
				fflucExists = true;
			} else {
				// 余計な部分が設定ファイルに書かれている場合
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

	@Override
	public void writeConfig(Path path, String... configs) {
		// 書き込めるように入力値をlinesに格納
		List<String> lines = new ArrayList<String>();
		for (String str : configs) {
			// 設定値を表す文字列であるかをチェック、関係ない行は無視
			if (str.startsWith(DSTACK) || str.startsWith(FFLUC) || str.startsWith(DFLUC) || str.startsWith(SFLUC)) {
				lines.add(str);
			}
			// 記載のない設定項目はloadでデフォルトに設定されるのでここでは処理しない
		}

		// 書き込み(上書き)
		try {
			System.out.println(lines);
			Path p = Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
			System.out.println(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * ユーティリティメソッド
	 */

	// 与えられたパスのテキストファイルを1行ごとにList<String>で返す
	private List<String> loadFile(Path path) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(path);
		} catch (IOException e) {
			// TODO:読み込み失敗時の処理
			System.out.println("ファイルの読み込みに失敗しました");
			e.printStackTrace();
		}
		return lines;
	}

}
