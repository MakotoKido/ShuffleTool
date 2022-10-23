package com.example.shuffletool.entity;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

@Component
@Data
@SessionScope
public class ShuffleHistory implements Serializable {
	private static final long serialVersionUID = 1L;
	// シャッフル履歴を保持
	private List<String> history;
}
