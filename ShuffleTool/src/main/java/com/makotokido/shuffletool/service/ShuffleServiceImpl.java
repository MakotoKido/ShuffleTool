package com.makotokido.shuffletool.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.makotokido.shuffletool.entity.Config;
import com.makotokido.shuffletool.entity.DeckList;
import com.makotokido.shuffletool.entity.ShuffleHistory;

// シャッフル関連のメソッドを実装したクラス
@Service
public class ShuffleServiceImpl implements ShuffleService {
	@Autowired
	Config conf;
	@Autowired
	ShuffleHistory history;
	// シャッフル方法のキーワード
	private final String DEAL = "deal"; // ディールシャッフル
	private final String HINDU = "hindu"; // ヒンズーシャッフル
	private final String FARO = "faro"; // ファローシャッフル

	@Override
	public void shuffle(DeckList decklist, String shuffle) {
		// シャッフル結果が存在するか判定
		List<String> list;
		if (decklist.getResult() == null) {
			// シャッフル処理が初回の場合、読み込んだデッキリストをシャッフル用にコピー
			list = new ArrayList<String>(decklist.getOriginal());
		} else {
			// シャッフル結果がすでに存在している場合、そのシャッフル結果をシャッフルに使用
			list = decklist.getResult();
		}

		// 与えられたシャッフル方法に応じてメソッドを呼び出す
		if (DEAL.equals(shuffle)) {
			// ディールシャッフルを行う
			list = dealShuffle(list);
			addHistory("ディールシャッフル");
		} else if (HINDU.equals(shuffle)) {
			// ヒンズーシャッフルを行う
			list = hinduShuffle(list);
			addHistory("ヒンズーシャッフル");
		} else if (FARO.equals(shuffle)) {
			// ファローシャッフルを行う
			list = faroShuffle(list);
			addHistory("ファローシャッフル");
		}
		// シャッフル結果を格納
		decklist.setResult(list);
	}

	/*
	 * シャッフルメソッド
	 */
	// ディールシャッフルを行う
	private List<String> dealShuffle(List<String> list) {
		/*
		 *  設定された個数の山に、デッキから0-2枚ずつ(標準1枚、設定した確率で0枚か2枚になる)乗せ、並べた順に山をまとめる
		 */
		// 設定値を取得
		int stacks = conf.getDealStacks(); // 作る山の個数
		int fluc = conf.getDealFluc(); // 枚数がぶれる確率(単位:%)

		// 分けた山に割り振られたカードのインデックスを保持するmapを定義
		// 山ごとの正確な枚数が予測できないため、一旦結果をStringBuilderに格納する(,を区切り文字とする)
		Map<Integer, StringBuilder> stk = new HashMap<Integer, StringBuilder>();
		for (int i = 0; i < stacks && i < list.size(); i++) {
			// 山の個数がデッキ枚数より多く設定されている場合は山の個数はデッキ枚数と同じにする
			stk.put(i, new StringBuilder());
		}

		/*
		 *  デッキの上から1枚ずつ、それぞれの山に定義したMapにインデックス番号を格納する
		 *  人力でシャッフルする際と同じく、デッキの上にあったカードほど山の下になるよう配置する
		 */
		for (int j = 0; j < list.size();) {
			for (int i = 0; i < stk.size(); i++) {
				StringBuilder index = stk.get(i);
				// カードを割り振る枚数を乱数で決定
				int rand = new Random().nextInt(100);
				// 割り振る枚数
				int take = 0;
				if (rand >= fluc) {
					// 乱数がfluc以上の場合1枚とる
					take = 1;
				} else if (rand >= fluc / 2 && rand < fluc) {
					// 乱数がfluc未満かつflucの半分(小数点以下切り捨て)以上の場合2枚
					take = 2;
				} else {
					// 乱数がflucの半分未満の場合は1枚も取らない
				}
				// カードを割り振る
				for (int k = 0; k < take; k++) {
					// カードを配った枚数がデッキの枚数を超えないようにチェック
					if (j < list.size()) {
						index.insert(0, j + ",");
						j++;
					}
				}
			}
		}

		// 出来上がった配列を組み合わせる
		int[] order = new int[0];
		for (int i = 0; i < stk.size(); i++) {
			String[] str = stk.get(i).toString().split(",");
			if (!"".equals(str[0])) {
				// 空文字の配列は処理しない
				int[] arr = Stream.of(str).mapToInt(Integer::parseInt).toArray();
				order = concatArray(order, arr);
			}
		}

		// 作成した配列の通りに山札の並びを変える
		list = sortList(list, order);
		return list;
	}

	// ヒンズーシャッフルを行う
	private List<String> hinduShuffle(List<String> list) {
		/*
		 *  デッキの下から、デッキの枚数の半分±設定値(デフォルトはデッキの13%)以内のランダムの枚数を取り、デッキの上に乗せる
		 */
		// 設定値を取得
		int flucrate = conf.getSplitFluc(); // 取る山の枚数がちょうど半分からブレる割合(単位:%)
		// デッキの枚数
		int count = list.size();

		// デッキを2つに分ける
		Map<String, int[]> stacks = splitDeck(count, flucrate);
		// デッキの上下の並びを表す配列を取得
		int[] top = stacks.get("top");
		int[] bottom = stacks.get("bottom");
		// 作成した配列をデッキの上下を反対にして組み合わせる
		int[] order = concatArray(bottom, top);

		// 作成した配列の通りに山札の並びを変える
		list = sortList(list, order);
		return list;
	}

	// ファローシャッフルを行う
	private List<String> faroShuffle(List<String> list) {
		/*
		 *  デッキの下から、デッキの枚数の半分±設定値(デフォルトはデッキの13%)以内のランダムの枚数を取り、できた2つの山のカードを互いに間に挟み込むようにして組み合わせる。
		 *  間に挟まるカードの枚数は標準1枚、設定した確率で2,3枚挟まる
		 */

		// 設定値を取得
		int sFluc = conf.getSplitFluc(); // 取る山の枚数がちょうど半分からブレる割合(単位:%)
		int fFluc = conf.getFaroFluc(); // 山を組み合わせる際に、間に挟まるカードが2,3枚のいずれかになる確率(単位:%)
		// デッキの枚数
		int count = list.size();

		// デッキを2つに分ける
		Map<String, int[]> stacks = splitDeck(count, sFluc);
		// デッキの上下の並びを表す配列を取得
		int[] top = stacks.get("top");
		int[] bottom = stacks.get("bottom");

		// 分割したデッキを、基本的に交互に重ねていく
		// 結果を格納する配列
		int[] order = new int[count];
		// 上の束の処理であることを示すフラグ
		boolean fromTop;
		// 上下どちらが組み合わせた束の一番上になるかはランダムに決める
		int rand = new Random().nextInt(2);
		if (rand < 1) {
			fromTop = true;
		} else {
			fromTop = false;
		}

		// 上下すべての山がなくなるまで互いを重ねる
		int t = 0; // 上からとった山のカーソル
		int b = 0; // 下からとった山のカーソル
		for (; t < top.length || b < bottom.length;) {
			// 重ねる枚数を決める
			rand = new Random().nextInt(100);
			int take = 0;
			if (rand >= fFluc) {
				// 乱数がfFluc以上の場合、重ねるカードは1枚
				take = 1;
			} else if (rand < fFluc && rand >= fFluc / 2) {
				// 乱数がfFluc未満かつfFlucの半分以上の場合、重ねるカードは3枚
				take = 3;
			} else {
				// 乱数がfFlucの半分未満の場合、重ねるカードは2枚
				take = 2;
			}
			// 決定した枚数カードを重ねる
			for (int i = 0; i < take; i++) {
				if (fromTop) {
					if (t < top.length) {
						// 上からとる
						order[t + b] = top[t];
						t++;
					}
				} else {
					if (b < bottom.length) {
						// 下からとる
						order[t + b] = bottom[b];
						b++;
					}
				}
			}
			// 上下の処理を次回は処理していないほうから行う
			fromTop = !fromTop;
		}

		// 作成した配列の通りに山札の並びを変える
		list = sortList(list, order);
		return list;
	}

	/*
	 * 共通ユーティリティメソッド
	 */
	// リストとその要素のインデックスを並べ替えた配列を与え、配列に記されたインデックスの順番に並べ替えたリストを返す
	private List<String> sortList(List<String> list, int[] arr) {
		// 与えられた配列とリストの大きさが一致するかチェック
		if (arr.length == list.size()) {
			// 並べ替え結果を格納するList
			List<String> result = new ArrayList<String>();
			for (int i : arr) {
				// 与えられたListのi番目の要素を追加していく
				result.add(list.get(i));
			}
			// 結果をlistに上書き
			list = new ArrayList<String>(result);
		} else {
			// 与えられた配列とリストの大きさが一致しない場合、処理を行わない
		}
		return list;
	}

	// 与えられた任意の個数のint配列を第1引数から順に合わせて一つの配列にして返す
	private int[] concatArray(int[]... arrs) {
		// 与えられた配列の大きさ合計
		int length = 0;
		for (int[] arr : arrs) {
			length += arr.length;
		}

		// 返却する配列を作成
		int[] result = new int[length];
		int cursor = 0;
		for (int[] arr : arrs) {
			System.arraycopy(arr, 0, result, cursor, arr.length);
			cursor += arr.length;
		}
		return result;
	}

	/*
	 *  デッキを2つに分け、上側と下側のデッキの並びをmap(keyはtop, bottom)で返す
	 *  count デッキの枚数, flucrate 取る山の枚数がちょうど半分からブレる割合(単位:%)
	 */
	private Map<String, int[]> splitDeck(int count, int flucrate) {
		// デッキからとる枚数のブレの最大値(小数点以下切り捨て）
		int fluc = count * flucrate / 100;
		// デッキの下からとる枚数を決定
		Random rand = new Random();
		int stack = count / 2 - fluc + rand.nextInt(fluc * 2 + 1);

		// デッキの並びを表した配列を作成
		int[] bottom = new int[stack]; // 山札の下部分（乗せた後に上になる）
		int[] top = new int[count - stack]; // 山札の上部分(乗せた後下になる)

		// それぞれの配列にインデックスを割り当てる
		for (int i = 0; i < count; i++) {
			if (i < count - stack) {
				top[i] = i;
			} else {
				bottom[i - (count - stack)] = i;
			}
		}

		// デッキを分割した結果をmapに格納する
		Map<String, int[]> map = new HashMap<String, int[]>();
		map.put("top", top);
		map.put("bottom", bottom);
		return map;
	}

	// 引数をシャッフル履歴にセット
	private void addHistory(String txt) {
		// シャッフル履歴が存在するか判定
		if (history.getHistory() == null) {
			// シャッフル履歴が存在しない場合、初期化
			history.setHistory(new ArrayList<String>());
			history.getHistory().add(txt);
		} else {
			// シャッフル履歴が存在している場合、そこに引数を追加
			history.getHistory().add(txt);
		}
	}

}
