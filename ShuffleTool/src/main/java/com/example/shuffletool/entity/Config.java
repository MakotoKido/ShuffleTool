package com.example.shuffletool.entity;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

@Component
@Data
@SessionScope
public class Config implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// ディールシャッフル
	// 作る山の個数
	@NotNull
	@Range(min=3)
	private int dealStacks;
	// 山に配るカードの枚数がブレる確率(単位:%)
	@NotNull
	@Range(min=0,max=100)
	private int dealFluc;
	
	// ヒンズーシャッフル・ファローシャッフル共通
	// 分ける山の枚数が半分からブレる枚数のデッキに対する割合(単位:%)
	@NotNull
	@Range(min=0,max=100)
	private int splitFluc;
	
	// ファローシャッフル
	// 山を組み合わせる際に、間に挟まるカードが0,2,3枚のいずれかになる確率(単位:%)
	@NotNull
	@Range(min=0,max=100)
	private int faroFluc;
}
