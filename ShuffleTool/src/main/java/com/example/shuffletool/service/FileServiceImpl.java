package com.example.shuffletool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {

//	与えられたパスのテキストファイルをList<String>で返す
	@Override
	public List<String> loadFile(Path path) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(path);
		} catch (IOException e) {
			System.out.println("ファイルの読み込みに失敗しました");
			e.printStackTrace();
		}
		return lines;
	}

}
