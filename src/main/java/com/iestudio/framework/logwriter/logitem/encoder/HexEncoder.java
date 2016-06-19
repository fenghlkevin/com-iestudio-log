package com.iestudio.framework.logwriter.logitem.encoder;





import com.iestudio.framework.logwriter.logitem.ILogEncoder;
import com.iestudio.framework.logwriter.util.MessyCodeCheck;
import com.kevin.iesutdio.tools.base64.SBase64;

public class HexEncoder implements ILogEncoder {

	@Override
	public String execute(Object obj) {
		if(obj instanceof String){
			String s=(String)obj;
			if(MessyCodeCheck.isMessyCode(s)){		
				return SBase64.encode(s.getBytes());
			}else{
				return s;
			}
		}
		if(obj==null){
			return "";
		}else{
			return obj.toString();
		}
		
	}

//	private String bytesToHexString(byte[] bArray) {
//		StringBuffer sb = new StringBuffer(bArray.length);
//		String sTemp;
//		for (int i = 0; i < bArray.length; i++) {
//			sTemp = Integer.toHexString(0xFF & bArray[i]);
//			if (sTemp.length() < 2)
//				sb.append(0);
//			sb.append(sTemp.toUpperCase());
//		}
//		return sb.toString();
//	}
}
