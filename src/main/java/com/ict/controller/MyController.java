package com.ict.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ict.service.MyService;
import com.ict.vo.CVO;
import com.ict.vo.MVO;
import com.ict.vo.VO;

@Controller
public class MyController {
	@Autowired
	private MyService myService;
	
	@RequestMapping("product_list.do")
	public ModelAndView listCommand(HttpServletRequest request) {
		try {
			ModelAndView mv = new ModelAndView("product_list");
			String category = request.getParameter("category");
			// 첫번째 들어올 때는 파라미터가 null(처음에는 category가 없으므로)
			if(category == null) { // 카테고리를 직접 지정
				category = "ele002";
			}
			List<VO> list = myService.selectShopList(category);
			mv.addObject("list", list);
			return mv;
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping("onelist.do")
	public ModelAndView onelistCommand(@RequestParam("idx")String idx) {
		try {
			ModelAndView mv = new ModelAndView("product_content");
			VO vo = myService.selectShopOneList(idx);
			mv.addObject("vo", vo);
			return mv;
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping("login.do")
	public ModelAndView loginCommand() {
		return new ModelAndView("login");
	}
	
	@RequestMapping("login_ok.do")
	public ModelAndView loginOkCommand(MVO m_vo, HttpSession session) {
		try {
			MVO mvo = myService.selectShopLogIn(m_vo);
			if(mvo == null) { // id나 pw가 없는 경우
				return new ModelAndView("login_err");
			} else {
				// 로그인 정보는 세션에 저장한다
				session.setAttribute("mvo", mvo); // id, pw 저장
				// login했다는 것을 구별하기위해 login이라는 이름에 ok라는 값을 가지면 로그인됨
				session.setAttribute("login", "ok"); 
				
				// 관리자인 경우
				if(mvo.getId().equals("admin")&&mvo.getPw().equals("admin")) {
					session.setAttribute("admin", "ok");
					return new ModelAndView("admin");
				}
				// 관리자가 아니면 일반 회원
				return new ModelAndView("redirect:product_list.do"); // category를 안가지고 가므로 기본 카테고리인 ele002로 갈 것임	
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping("logout.do")
	public ModelAndView logoutCommand(HttpSession session) {
		session.invalidate(); // 세션을 완전히 삭제
		return new ModelAndView("redirect:product_list.do");
	}
	
	/*
	@RequestMapping("addCart.do")
	public ModelAndView addCartCommand(@ModelAttribute("idx")String idx,
			HttpSession session) {
		try {
			ModelAndView mv = new ModelAndView("redirect:onelist.do");
			MVO mvo = (MVO)session.getAttribute("mvo");
			String id = mvo.getId();
			
			// idx를 이용해서 제품 정보를 구하자
			VO vo = myService.selectShopOneList(idx);
			
			// 해당 아이디를 가진 사람의 카트에 해당 제품이 있는지 검색
			// 없으면 카트에 추가, 있으면 카트에 해당 제품의 수량을 1증가
			CVO cvo = myService.selectShopCartSearch(id, vo.getP_num());
			if(cvo == null) { 
				// 카트에 없으므로 추가
				CVO c_vo = new CVO();
				c_vo.setP_num(vo.getP_num());
				c_vo.setP_name(vo.getP_name());
				c_vo.setP_price(String.valueOf(vo.getP_price()));
				c_vo.setP_saleprice(String.valueOf(vo.getP_saleprice()));
				c_vo.setAmount(String.valueOf(1));
				c_vo.setId(id);
				int result = myService.insertShopCartAdd(c_vo);
			} else { 
				// 카트에 있으므로 수량 증가
				int result = myService.updateShopCartUp(cvo);
			}
			
			return mv;
		} catch (Exception e) {
		}
		return null;
	}
	*/
	
	// ajax 사용
	@RequestMapping(value = "addCart.do", produces = "application/html; charset=utf-8")
	@ResponseBody
	public String addCartCommand(@ModelAttribute("idx")String idx,
			HttpSession session) {
		try {
			MVO mvo = (MVO)session.getAttribute("mvo");
			String id = mvo.getId();
			
			// idx를 이용해서 제품 정보를 구하자
			VO vo = myService.selectShopOneList(idx);
			
			// 해당 아이디를 가진 사람의 카트에 해당 제품이 있는지 검색
			// 없으면 카트에 추가, 있으면 카트에 해당 제품의 수량을 1증가
			CVO cvo = myService.selectShopCartSearch(id, vo.getP_num());
			int result = 0;
			if(cvo == null) { 
				// 카트에 없으므로 추가
				CVO c_vo = new CVO();
				c_vo.setP_num(vo.getP_num());
				c_vo.setP_name(vo.getP_name());
				c_vo.setP_price(String.valueOf(vo.getP_price()));
				c_vo.setP_saleprice(String.valueOf(vo.getP_saleprice()));
				c_vo.setAmount(String.valueOf(1));
				c_vo.setId(id);
				result = myService.insertShopCartAdd(c_vo);
			} else { 
				// 카트에 있으므로 수량 증가
				result = myService.updateShopCartUp(cvo);
			}
			return String.valueOf(result); // 성공하면 1이 리턴됨
		} catch (Exception e) {
			return "0";
		}
	}
	
	@RequestMapping("showCart.do")
	public ModelAndView showCartCommand(HttpSession session) {
		try {
			ModelAndView mv = new ModelAndView("viewcart");
			MVO mvo = (MVO)session.getAttribute("mvo");
			String id = mvo.getId();
			// 카트 테이블에서 해당 아이디가 가진 모든 목록을 가져오기 
			List<CVO> cartList = myService.selectShopCartList(id);
			mv.addObject("cartList", cartList);
			return mv;
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping("editCart.do")
	public ModelAndView editCartCommand(CVO cvo) {
		try {
			int result = myService.updateCartAmount(cvo);
			return new ModelAndView("redirect:showCart.do");
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping("deleteCart.do")
	public ModelAndView deleteCartCommand(CVO cvo) {
		try {
			int result = myService.deleteCartDel(cvo);
			return new ModelAndView("redirect:showCart.do");
		} catch (Exception e) {
		}
		return null;
	}
	
	@RequestMapping(value = "product_add", method = RequestMethod.POST)
	public ModelAndView productAddCommand(VO vo, HttpServletRequest request) {
		try {
			ModelAndView mv = new ModelAndView("redirect:product_list.do?category=" + vo.getCategory());
			String path = request.getSession().getServletContext().getRealPath("/resources/images");
			MultipartFile file_1 = vo.getP_image1();
			MultipartFile file_2 = vo.getP_image2();
			
			vo.setP_image_s(file_1.getOriginalFilename());
			vo.setP_image_l(file_2.getOriginalFilename());
			int result = myService.inserShopProductAdd(vo);
			if(result > 0) {
				// 2개라 2번 써야됨(여러개면 반복문 사용하는게 좋음)
				byte[] in = file_1.getBytes();
				File out = new File(path, vo.getP_image_s());
				FileCopyUtils.copy(in, out);
				
				byte[] in2 = file_2.getBytes();
				File out2 = new File(path, vo.getP_image_l());
				FileCopyUtils.copy(in, out2);
			}
			return mv;
		} catch (Exception e) {
		}
		return null;
	}
}