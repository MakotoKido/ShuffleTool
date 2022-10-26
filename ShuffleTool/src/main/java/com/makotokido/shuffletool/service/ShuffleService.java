package com.makotokido.shuffletool.service;

import com.makotokido.shuffletool.entity.DeckList;

// シャッフルを行うサービスクラス
public interface ShuffleService {
	//	与えられたシャッフル方法のメソッドを呼び出す
	void shuffle(DeckList decklist, String shuffle);
}
