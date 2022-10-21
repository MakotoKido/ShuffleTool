package com.example.shuffletool.controller;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.shuffletool.ShuffleToolApplication;
import com.example.shuffletool.entity.Config;
import com.example.shuffletool.entity.DeckList;
import com.example.shuffletool.service.FileService;
import com.example.shuffletool.service.ShuffleService;

@Controller
public class RequesrtParamController {
	// 必要なインスタンスの生成
	@Autowired
	private FileService fileservice;
	@Autowired
	private ShuffleService shuffleservice;
	@Autowired
	private DeckList decklist;
	@Autowired
	private Config config;

	/*
	 * 最初に表示するページを取得 デッキリストの入力を行う
	 */
	@GetMapping("entry")
	public String resultView(Model model) {
		// TODO:適宜マッピングも修正
		// デッキリストのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("decklist.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// デッキリストを読み込み、エンティティに保持
		fileservice.loadDeck(path);

		// シャッフルの設定値を読み込み
		// 設定ファイルのパスを取得
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// 設定ファイル読み込み
		fileservice.loadConfig(path);
		// 読み込み結果をビューで表示するための準備
		model.addAttribute(decklist);
		// TODO:戻り値要検討
		return "result";
	}

	/*
	 * シャッフル結果を表示
	 */
	// シャッフル方法を受け取りシャッフル後のデッキリストを表示
	@PostMapping("result")
	public String resultShuffle(@RequestParam String shuffle, Model model) {
		// 与えた方法でシャッフルを行う
		shuffleservice.shuffle(decklist, shuffle);
		// シャッフル結果を格納
		model.addAttribute(decklist);
		// TODO:シャッフル履歴を表示したい

		return "result";
	}

	// TODO:シャッフルをリセットするリクエストを受け取るメソッド追加→htmlにもボタン実装

	/*
	 * 設定を行う
	 */
	// 設定値を入力する画面に遷移
	@GetMapping("conf")
	public String configView() {
		// TODO:必要に応じて引数をつけて現在の設定値をvalueにセット
		return "config";
	}

	// 画面に入力された設定値を受け取り、ファイルに書き込んでエンティティに保持する
	@PostMapping("conf")
	public String setConfig(Model model, @RequestParam String dealstacks, @RequestParam String dealfluc,
			@RequestParam String farofluc, @RequestParam String splitfluc) {
		// TODO:ばりでーしょん(別クラスになるだろうけど)
		// 設定ファイルのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// ファイルを書き込み
		fileservice.writeConfig(path, dealstacks, dealfluc, farofluc, splitfluc);
		// 書き込んだ内容をエンティティに保持
		fileservice.loadConfig(path);
		// シャッフル画面に表示するmodelを設定
		model.addAttribute(decklist);
		// シャッフル画面に戻る
		return "result";
	}

}
