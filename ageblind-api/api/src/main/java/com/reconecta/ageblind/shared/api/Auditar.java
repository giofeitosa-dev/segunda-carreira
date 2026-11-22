package com.reconecta.ageblind.shared.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método de controller que deve ser auditado no log de segurança.
 * A ação é especificada pelo atributo {@link #acao()} (ex.: PERFIL_CRIADO,
 * PERFIL_ATUALIZADO, ACESSO_NEGADO).
 *
 * <p>O {@link AuditoriaAspect} intercepta métodos anotados e grava um registro
 * em auditoria_seguranca com falha-segura (nunca derruba a requisição principal).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditar {

	/** Tipo de ação executada (ex.: PERFIL_CRIADO, ACESSO_NEGADO). */
	String acao();

	/** Recurso acessado (pode ser deixado em branco para usar o caminho da requisição). */
	String recurso() default "";
}
