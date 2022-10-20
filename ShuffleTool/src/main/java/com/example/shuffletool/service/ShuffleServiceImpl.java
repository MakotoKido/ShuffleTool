package com.example.shuffletool.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.shuffletool.entity.DeckList;

// シャッフル関連のメソッドを実装したクラス
@Service
public class ShuffleServiceImpl implements ShuffleService {
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
			// TODO:タイムアウトしたとき用のエラー処理がいるかもしれない
			list = new ArrayList<String>(decklist.getOriginal());
		} else {
			// シャッフル結果がすでに存在している場合、そのシャッフル結果をシャッフルに使用
			list = decklist.getResult();
		}

		// 与えられたシャッフル方法に応じてメソッドを呼び出す
		if (DEAL.equals(shuffle)) {
			// ディールシャッフルを行う
			list = dealShuffle(list);
		} else if (HINDU.equals(shuffle)) {
			// ヒンズーシャッフルを行う
			list = hinduShuffle(list);
		} else if (FARO.equals(shuffle)) {
			// ファローシャッフルを行う
			list = faroShuffle(list);
		} else {
			// TODO:定義されていない値が返ってきた場合のエラー処理を定義
		}

		// シャッフル結果を格納
		decklist.setResult(list);
	}

	/*
	 * シャッフルメソッド
	 */

	private List<String> dealShuffle(List<String> list) {
		// 設定された個数(デフォルト7個)山を作り、その上にデッキから0-2枚ずつ(標準1枚、設定した確率(デフォルト5%)で0枚か2枚になる)乗せ、並べた順に山をまとめる
		// TODO:設定値(一旦固定)
		int stacks = 7; // 作る山の個数
		int flucrate = 5; // 枚数のぶれる確率(単位:%)

		// 分けた山に割り振られたカードのインデックスを保持するmapを定義
		// 山ごとの正確な枚数が予測できないため、一旦結果をStringBuilderに格納する(,を区切り文字とする)
		Map<Integer, StringBuilder> map = new HashMap<Integer, StringBuilder>();
		for (int i = 0; i < stacks; i++) {
			map.put(i, new StringBuilder());
		}

		// デッキの上から1枚ずつ、それぞれの山に定義したMapにインデックス番号を格納する
		// 人力でシャッフルする際と同じく、デッキの上にあったカードほど出来上がった山の下に配置する
		for (int j = 0; j < list.size();) {
			for (int i = 0; i < stacks; i++) {
				// デッキの個数と山の個数によってはデッキの枚数以上にカードを取ってしまうので再度チェック
				if (j < list.size()) {
					StringBuilder index = map.get(i);
					// カードを割り振る枚数を乱数で決定
					// 乱数がブレの数値未満かつブレの数値の半分(小数点以下切り捨て)以上の場合2枚、それより小さい場合は1枚も取らない
					int rand = new Random().nextInt(100);
					// 割り振る枚数
					int take = 0;
					if (rand >= flucrate) {
						take = 1; // 1枚割り振る
					} else if (rand >= flucrate / 2 && rand < flucrate) {
						take = 2; // 2枚割り振る
					} else {
						// 0枚割り振る
					}
					// カードを割り振る
					for (int k = 0; k < take; k++) {
						// 1枚目がデッキの最後の場合、2枚目でカードを追加しないようチェック
						if (j < list.size()) {
							index.insert(0, j + ",");
							j++;
						}
					}
				}
			}
		}

		// 出来上がった配列を組み合わせる
		int[] order = new int[0];
		for (int i = 0; i < map.size(); i++) {
			String[] str = map.get(i).toString().split(",");
			int[] arr = Stream.of(str).mapToInt(Integer::parseInt).toArray();
			order = concatArray(order, arr);
		}

		// 作成した配列の通りに山札の並びを変える
		list = sortList(list, order);
		return list;
	}

	private List<String> hinduShuffle(List<String> list) {
		// デッキの下から、デッキの枚数の半分±設定値(デフォルトはデッキの13%)以内のランダムの枚数を取り、デッキの上に乗せる
		// デッキの枚数
		int count = list.size();
		// TODO:設定値は一旦定数で
		// 取る山の枚数がちょうど半分からブレる割合(単位:%)
		int flucrate = 13;

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

	private List<String> faroShuffle(List<String> list) {
		// TODO:デッキの下から、デッキの枚数の半分±設定値(デフォルトはデッキの13%)以内のランダムの枚数を取り、できた2つの山のカードを互いに間に挟み込むようにして組み合わせる。
		// 間に挟まるカードの枚数は標準1枚、設定した確率で0,2,3枚挟まる

		// デッキの枚数
		int count = list.size();
		// TODO:設定値は一旦定数で
		// 取る山の枚数がちょうど半分からブレる割合(単位:%)
		int flucrate = 13;
		// 山を組み合わせる際に、間に挟まるカードが0,2,3枚のいずれかになる確率(単位:%)
		int btwfluc = 20;

		// デッキを2つに分ける
		Map<String, int[]> stacks = splitDeck(count, flucrate);
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
			// TODO:説明
			if (rand >= btwfluc) {
				take = 1;
			} else if (rand < btwfluc && rand >= btwfluc * 2 / 3) {
				take = 3;
			} else if (rand < btwfluc && rand >= btwfluc * 2 / 3) {
				take = 2;
			}

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
			fromTop = !fromTop;
		}

		// 作成した配列の通りに山札の並びを変える
		list = sortList(list, order);
		return list;
	}

	/*
	 * 共通ユーティリティメソッド
	 */

	// 配列2つとリストを与え、配列2つを1→2の順に組み合わせた順番にリストを並べ替えて返す
	private List<String> sortList(List<String> list, int[] arr) {
		// 与えられた配列とリストの大きさが一致するか確認
		if (arr.length == list.size()) {
			// 並べ替えを行う
			// 並べ替え結果を格納するList
			List<String> result = new ArrayList<String>();

			for (int i : arr) {
				// 与えられたListのi番目の要素を追加していく
				result.add(list.get(i));
			}
			// 結果をlistに上書き
			list = new ArrayList<String>(result);
		} else {
			// 並べ替えを行わない
			System.out.println("error in size");
			// TODO:エラー時の処理を定義
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

	// デッキを2つに分け、上側と下側のデッキの並びをmap(keyはtop, bottom)で返す
	// count デッキの枚数, flucrate 取る山の枚数がちょうど半分からブレる割合(単位:%)
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

}
