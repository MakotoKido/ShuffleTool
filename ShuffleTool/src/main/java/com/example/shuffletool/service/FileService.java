package com.example.shuffletool.service;

import java.nio.file.Path;
import java.util.List;

public interface FileService {

//	与えられたパスのファイルを読み込み、List<String>で返却する
//	ファイルの入力は後からアップロード形式に変更予定のため、Deprecated
	@Deprecated
	public List<String> loadFile(Path path);
	
	
	
}
