package com.example.shuffletool.service;

import com.example.shuffletool.entity.DeckList;

// サービス処理
public interface ShuffleService {
	//	与えられたシャッフル方法のメソッドを呼び出す
	void shuffle(DeckList decklist, String shuffle);
}
