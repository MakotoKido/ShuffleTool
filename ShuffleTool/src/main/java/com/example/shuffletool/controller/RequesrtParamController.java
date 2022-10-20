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
import com.example.shuffletool.entity.DeckList;
import com.example.shuffletool.service.FileService;
import com.example.shuffletool.service.ShuffleService;

@Controller
public class RequesrtParamController {
	// パス固定で一旦デッキリストを読み込ませて進めていく
	@Autowired
	private FileService fileservice;

	@Autowired
	private ShuffleService shuffleservice;

	@Autowired
	 private DeckList decklist;

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
		
		// 読み込み結果をビューで表示するための準備
		model.addAttribute(decklist);

		// TODO:戻り値要検討
		return "result";
	}
	
	// TODO:シャッフルをリセットするリクエストを受け取るメソッド追加→htmlにもボタン実装

	// シャッフル方法を受け取り処理
	@PostMapping("result")
	public String resultShuffle(@RequestParam String shuffle, Model model) {
		// 与えた方法でシャッフルを行う
		shuffleservice.shuffle(decklist, shuffle);
		// シャッフル結果を格納
		model.addAttribute(decklist);
//		TODO:シャッフル履歴を表示したい

		return "result";
	}
	
	// 設定を読み込み
	// TODO:動作確認のため仮、初期化で使用した方がいいと思ったまる
	@GetMapping("conf")
	public String configView(Model model) {
		// 設定ファイルのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// 設定ファイル読み込み
		fileservice.loadConfig(path);
		
		// シャッフル画面に戻る
		// modelは与えないとぬるぽになる
		model.addAttribute(decklist);
		return "result";
	}

}
