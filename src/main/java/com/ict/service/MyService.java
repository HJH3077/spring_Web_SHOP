package com.ict.service;

import java.util.List;

import com.ict.vo.CVO;
import com.ict.vo.MVO;
import com.ict.vo.VO;

public interface MyService {
	MVO selectShopLogIn(MVO mvo) throws Exception;
	
	List<VO> selectShopList(String category) throws Exception;
	
	VO selectShopOneList(String idx) throws Exception;
	
	int inserShopProductAdd(VO vo) throws Exception;
	
	CVO selectShopCartSearch(String id, String p_num) throws Exception;
	int insertShopCartAdd(CVO cvo) throws Exception;
	int updateShopCartUp(CVO cvo) throws Exception;
	
	// 카트에서 수량을 변경하면 변경된 수량을 적용
	int updateCartAmount(CVO cvo) throws Exception;
	
	List<CVO> selectShopCartList(String id) throws Exception;
	
	int deleteCartDel(CVO cvo) throws Exception;
}