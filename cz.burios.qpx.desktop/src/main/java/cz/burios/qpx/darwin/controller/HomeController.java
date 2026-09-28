package cz.burios.qpx.darwin.controller;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	@GetMapping("/")
	public ModelAndView index() {
		ModelAndView view = new ModelAndView("index");
		try {
			java.util.Date now = new java.util.Date();
			view.addObject("timeNo", DateFormatUtils.format(now, "yyyyMMdd.HHmmssSSS"));
			view.addObject("appPath", "archer");
			/*
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		return view;
	}
}