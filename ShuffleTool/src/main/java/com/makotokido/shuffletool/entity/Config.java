package com.makotokido.shuffletool.entity;

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
	
	/*
	 *  ディールシャッフル
	 */
	// カードを配る山の個数
	@NotNull
	@Range(min=3, max=10)
	private Integer dealStacks;
	// 山に配るカードの枚数が0枚か2枚にブレる確率(単位:%)
	@NotNull
	@Range(min=0, max=100)
	private Integer dealFluc;
	
	/*
	 *  ファローシャッフル
	 */
	// 山を組み合わせる際に、間に挟まるカードが2枚か3枚にブレる確率(単位:%)
	@NotNull
	@Range(min=0, max=100)
	private Integer faroFluc;
	
	/*
	 *  ヒンズーシャッフル・ファローシャッフル共通
	 */
	// 分ける山の枚数がちょうど半分からブレる最大枚数のデッキに対する割合(単位:%)
	@NotNull
	@Range(min=0, max=100)
	private Integer splitFluc;
	

}
