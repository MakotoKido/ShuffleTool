package com.example.shuffletool.controller;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
	DeckList decklist;

	@GetMapping("entry")
	public String resultView(Model model) {
		// TODO:仮で作っているため、代替手段が見つかった場合は削除
		// TODO:適宜マッピングも修正
		// デッキリストのパスを取得
		Path path = null;
		try {
			path = Path.of(ShuffleToolApplication.class.getClassLoader().getResource("decklist.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// デッキリストの読み込み
		List<String> list = fileservice.loadFile(path);
		// 読み込んだデッキリストをエンティティに保持
		decklist.setOriginal(list);
		// 読み込み結果をビューで表示するための準備
		model.addAttribute(decklist);

		return "result";
	}

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

}
