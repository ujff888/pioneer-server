package cn.litgame.wargame.server.servlet;

import java.io.File;
import java.io.FileOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cn.litgame.wargame.core.auto.GameProtos.MessageBody;
import cn.litgame.wargame.server.logic.HttpMessageManager;
import cn.litgame.wargame.server.message.KHttpMessageContext;
import cn.litgame.wargame.server.message.SimpleKHttpMessage;

@Controller
public class GameServlet {
	
//	@Resource(name = "logManager")
//	private LogManager logManager;
	private final static Logger log = Logger.getLogger(GameServlet.class);
	private static boolean debug = true;
	
	public static boolean getDebug(){
		return debug;
	}
	
	@RequestMapping("/debug.lc")
	public void setDebug(HttpServletRequest request, HttpServletResponse response){
		String debugStr = request.getParameter("debug");
		debug = Integer.parseInt(debugStr) == 0 ?false :true;
	}
	
	@PostConstruct
	public void init(){
		try{
			debug =  new PropertiesConfiguration("conf.properties").getBoolean("debug");
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
	@Resource(name = "httpMessageManager")
	private HttpMessageManager httpMessageManager;
	
	/**
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/game.lc")
	public void initGame(HttpServletRequest request, HttpServletResponse response) throws Exception{
		int bodyLength = request.getContentLength();
		if(bodyLength == -1){
			response.sendRedirect("http://www.baidu.com");
			return;
		}
		String ip = request.getHeader("X-Real-IP");
		SimpleKHttpMessage message = new SimpleKHttpMessage(request.getInputStream(),response.getOutputStream(),ip,bodyLength);
		if(message.getCode() != 1){
			log.error("syn error close this request");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().flush();;
			return;
		}
		KHttpMessageContext context = new KHttpMessageContext(message);

		HttpMessageManager.setKHttpMessageContext(context);
		try{
			MessageBody mb = context.getSimpleKHttpMessage().getMessageBody();
			long startTime = System.currentTimeMillis();
			httpMessageManager.handler(mb);
			log.info("end handl message type : " + mb.getMessageType() +" use time: "+ (System.currentTimeMillis()-startTime));
		}catch(Exception e){
			log.error(e.getMessage(), e);
//				MessageContent.Builder builder = HttpMessageManager.getMessageContentBuilder();
//				builder.setMessageType(mc.getMessageType());
//				builder.setMessageCode(MessageCode.Err_Not_Known);
//				httpMessageManager.send(builder);
		}
	}
	
	@RequestMapping("/config.lc")
	public void reloadConfig(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
	}
	
	public static void main(String[] args) {
		System.out.println(137573173209L % 12);
	}
	
	/**
	 * {
		"message":"LuaScriptException: bad argument #3 to '?' (string expected, got nil)stack traceback:	[C]: at 0x45d9b650	[C]: in function '__newindex'	[string "E:/pioneer/client/project/pioneer/Assets/uLua..."]:330: in function 'setText'	[string "E:/pioneer/client/project/pioneer/Assets/uLua..."]:289: in function 'updateView'	[string "E:/pioneer/client/project/pioneer/Assets/uLua..."]:152: in function <[string "E:/pioneer/client/project/pioneer/Assets/uLua..."]:58>",
		"stacktrace":"LuaInterface.LuaFunction.call (System.Object[] args, System.Type[] returnTypes) (at Assets/uLua/Core/LuaFunction.cs:74)LuaInterface.LuaFunction.Call (System.Object[] args) (at Assets/uLua/Core/LuaFunction.cs:88)LuaScriptMgr.CallLuaFunction (System.String name, System.Object[] args) (at Assets/uLua/Source/Base/LuaScriptMgr.cs:597)CToLuaControl.CallMethod (System.String funcName, System.Object[] args) (at Assets/scripts/pioneerGame/control/CToLuaControl.cs:15)UIView.callLuaMethod (System.String func, System.Object[] args) (at Assets/scripts/pioneerGame/view/UIView.cs:39)UIView.Start () (at Assets/scripts/pioneerGame/view/UIView.cs:21)",
		"time":"2016-5-30 15:28:3"
		"device":"Intel(R) Core(TM) i5-4590 CPU @ 3.30GHz (8130 MB)
		}
	 * @param bug
	 * @param file
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/bug.lc")
	public void bug(@RequestParam("bug") String bug,@RequestParam("fileField") MultipartFile file,HttpServletResponse response) throws Exception{
		System.out.println("bug=============" + bug);
		System.out.println("file=============" + file.getBytes().length);
		if(!file.isEmpty()){
			String fileName = "e:/test.jpg";
			File png = new File(fileName);
			if(png.exists()){
				png.delete();
			}
			
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(file.getBytes());
			out.close();
		}
		response.getWriter().print("ok");
	}
	
}
