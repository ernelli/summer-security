package com.hencjo.summer.security.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Responder {
	enum ContinueOrRespond { RESPOND, CONTINUE}

	ContinueOrRespond respond(HttpServletRequest request, HttpServletResponse response) throws Exception;
	String describer();
}
