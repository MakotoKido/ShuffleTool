package com.example.shuffletool.controller;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	private Config conf;

	/*
	 * 最初に表示するページを取得 デッキリストの入力を行う
	 */
	// 各種値をファイルに保持している値に初期化、デッキリストの入力画面を表示
	@GetMapping("entry")
	public String entryView(Model model) {
		// デッキリストのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("decklist.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// デッキリストを読み込み、エンティティに保持
		fileservice.loadDeck(path);
		// 読み込んだデッキリストの改行入りStringを取得、初期値にセット
		String initial = fileservice.deckToString();
		model.addAttribute("deck", initial);

		// シャッフルの設定値を読み込み
		// 設定ファイルのパスを取得
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// 設定ファイル読み込み
		fileservice.loadConfig(path);
		// 入力画面へ
		return "entry";
	}

	// デッキリストの入力を受け取り、シャッフル画面を表示
	@PostMapping("entry")
	public String resultView(@RequestParam String deck, Model model) {
		// デッキリストのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("decklist.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// TODO:デッキリスト入力値のバリデーションを検討
		// 入力内容をファイルに書き込み
		fileservice.writeDeck(deck, path);
		// 書き込んだ内容を読み込み
		fileservice.loadDeck(path);
		// シャッフル画面に表示するデッキリストをセット
		// TODO:formと同様の挙動が作れるなら、addattribute不要説
		model.addAttribute(decklist);
		return "result";
	}

	/*
	 * シャッフル結果を表示
	 */
	// シャッフル方法を受け取りシャッフル後のデッキリストを表示
	@PostMapping("result")
	public String resultShuffle(@RequestParam String shuffle, Model model) {
		// TODO:ここでconfigが設定されていないとまずいので、初期化する必要がありそう(デッキリスト保持しててもなんか消えるし)
		if (0 == conf.getDealFluc()) {
			Path path = null;
			// シャッフルの設定値を読み込み
			// 設定ファイルのパスを取得
			try {
				path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			fileservice.loadConfig(path);
		}
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
	public String configView(Model model) {
		// 現在の設定値を表示させる
		model.addAttribute(conf);
		return "config";
	}

	// 画面に入力された設定値を受け取り、ファイルに書き込んでエンティティに保持する
	@PostMapping("conf")
	public String setConfig(Model model, @ModelAttribute @Validated Config conf, BindingResult result) {
		// TODO:ばりでーしょん、なぜか適用されない
		// 入力チェックされた場合
		if (result.hasErrors()) {
			// 設定画面に戻る
			System.out.println("入力値がおかしいぞい");
			return "config";
		}
		// 設定ファイルのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// ファイルを書き込み
		fileservice.writeConfig(path, conf);
		// 書き込んだ内容をエンティティに保持
		fileservice.loadConfig(path);
		// シャッフル画面に表示するmodelを設定
		model.addAttribute(decklist);
		// シャッフル画面に戻る
		return "result";
	}

	/*
	 * エラー画面から戻る
	 */
	@PostMapping("back")
	public String backEntry(Model model) {
		// TODO:エラー後なのでいろいろ初期化して初めに戻る
		model.addAttribute(decklist);
		return "result";
	}
}
