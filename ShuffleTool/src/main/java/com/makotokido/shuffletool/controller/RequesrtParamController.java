package com.makotokido.shuffletool.controller;

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

import com.makotokido.shuffletool.ShuffleToolApplication;
import com.makotokido.shuffletool.entity.Config;
import com.makotokido.shuffletool.entity.DeckList;
import com.makotokido.shuffletool.entity.ShuffleHistory;
import com.makotokido.shuffletool.service.FileService;
import com.makotokido.shuffletool.service.ShuffleService;

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
	@Autowired
	private ShuffleHistory history;

	// 各種ファイルのパス
	// デッキリスト
	Path deckpath = null;
	// 設定
	Path confpath = null;

	// 各種パスを設定
	private void setupPath() {
		try {
			deckpath = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("deck/decklist.txt").toURI());
			confpath = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("conf/config.txt").toURI());
		} catch (URISyntaxException e) {
			// TODO:エラー時の処理
			e.printStackTrace();
		} catch (Exception e) {
			// 予期せぬエラー
			e.printStackTrace();
		}
	}

	// TODO:初期化メソッドが欲しい、あとタイミング

	/*
	 * 最初に表示するページを取得 デッキリストの入力を行う
	 */
	// 各種値をファイルに保持している値に初期化、デッキリストの入力画面を表示
	@GetMapping("entry")
	public String entryView(Model model) {
		// 各種パスのセットアップ
		setupPath();
		// 読み込んだデッキリストの改行入りStringを取得、初期値にセット
		String initial = fileservice.deckToString(deckpath);
		model.addAttribute("deck", initial);
		// 設定ファイル読み込み
		fileservice.loadConfig(confpath);
		// 入力画面へ
		return "entry";
	}

	// デッキリストの入力を受け取り、シャッフル画面を表示
	@PostMapping("entry")
	public String resultView(@RequestParam String deck, Model model) {
		// デッキが空の場合、再入力		
		if(deck.equals("")) {
			return "entry";
		}
		// デッキリスト入力値をサニタイズ(番号を振ってスペースを入れる都合上必要)
		deck = saniImput(deck);
		// 入力内容をファイルに書き込み
		fileservice.writeDeck(deck, deckpath);
		// 書き込んだ内容を読み込み
		fileservice.loadDeck(deckpath);
		// シャッフル画面に表示するデッキリストとシャッフル履歴をセット
		model.addAttribute(decklist);
		model.addAttribute(history);
		return "result";
	}

	/*
	 * シャッフル結果を表示
	 */
	// シャッフル方法を受け取りシャッフル後のデッキリストを表示
	@PostMapping("result")
	public String resultShuffle(@RequestParam String shuffle, Model model) {
		// いずれの設定値も0を許容しないため、初期化されていない場合はファイルから改めて初期化を行う
		if (0 == conf.getDealFluc() || 0 == conf.getDealStacks() || 0 == conf.getFaroFluc()
				|| 0 == conf.getSplitFluc()) {
			fileservice.loadConfig(confpath);
		}
		// 与えた方法でシャッフルを行う
		shuffleservice.shuffle(decklist, shuffle);

		// シャッフル画面に表示するデッキリストとシャッフル履歴をセット
		model.addAttribute(decklist);
		model.addAttribute(history);
		return "result";
	}

	// シャッフル状態をリセット
	@PostMapping("reset")
	public String resetShuffle(Model model) {
		// シャッフル結果・履歴を削除
		decklist.setResult(null);
		history.setHistory(null);
		// シャッフル画面に表示するデッキリストとシャッフル履歴をセット
		model.addAttribute(decklist);
		model.addAttribute(history);
		return "result";
	}

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
	
	// 設定値をデフォルトに戻す
	@PostMapping("default")
	public String setDefault(Model model) {
		// 設定値をデフォルトに戻す
		fileservice.setDefault(confpath);
		// シャッフル画面に表示するデッキリストとシャッフル履歴をセット
		model.addAttribute(decklist);
		model.addAttribute(history);
		return "result";
	}

	// 画面に入力された設定値を受け取り、ファイルに書き込んでエンティティに保持する
	@PostMapping("conf")
	public String setConfig(@ModelAttribute @Validated Config conf, BindingResult result, Model model) {
		// 入力チェックされた場合
		if (result.hasErrors()) {
			// 設定画面に戻る
			return "config";
		}
		// ファイルを書き込み
		fileservice.writeConfig(confpath, conf);
		// 書き込んだ内容をエンティティに保持
		fileservice.loadConfig(confpath);
		// シャッフル画面に表示するデッキリストとシャッフル履歴をセット
		model.addAttribute(decklist);
		model.addAttribute(history);
		// シャッフル画面に戻る
		return "result";
	}

	/*
	 * エラー画面から戻る
	 */
	@PostMapping("back")
	public String backEntry(Model model) {
		// TODO:エラーをしっかり分岐させてあげる
		model.addAttribute(decklist);
		return "entry";
	}

	/*
	 * ユーティリティメソッド
	 */
	// 入力値をサニタイズ
	private String saniImput(String imp) {
		return imp.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
	}

}
