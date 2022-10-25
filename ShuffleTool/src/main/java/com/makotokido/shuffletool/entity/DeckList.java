package com.makotokido.shuffletool.entity;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

@Component
@Data
@SessionScope
public class DeckList implements Serializable {
	private static final long serialVersionUID = 1L;
	
//	読み込んだデッキリストを保持
	private List<String> original;
	
//	シャッフル後のデッキリストを保持
	private List<String> result;
}
