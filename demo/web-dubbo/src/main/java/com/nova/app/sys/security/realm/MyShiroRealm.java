package com.nova.app.sys.security.realm;


import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nova.app.user.domain.User;
import com.nova.app.user.service.UserService;

public class MyShiroRealm extends AuthorizingRealm {

	private Logger log = LoggerFactory.getLogger(MyShiroRealm.class);
	
	private static final String MESSAGE = "message";
	
	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if(principals == null){
            throw new AuthorizationException();
        }

        String userName = (String) principals.getPrimaryPrincipal();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        Set permissionsSet = userService.getPermissions(userName);
        info.setStringPermissions(permissionsSet);

        log.info("[ACCOUNT："+userName+"][PERMISSIONS："+permissionsSet+"]");
        return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;  
        String username = token.getUsername();
        String passwrod = null;
        if(token.getPassword() != null) {
        	passwrod = new String(token.getPassword());
        }
        if(username == null || "".equals(username)) {
        	this.setAttribute(MESSAGE, "用户名不能为空");
        	log.info("用户名为空");
        	return null;
        }
        if(passwrod == null || "".equals(passwrod)) {
        	this.setAttribute(MESSAGE, "密码不能为空");
        	log.info("密码为空");
        	return null;
        }
        User user = null;
        if(token.getUsername() != null && !"".equals(token.getUsername())) {
        	user = userService.getUserAnthenticaition(username, passwrod);
        }
        try {
        	return new SimpleAuthenticationInfo(user.getUserName(),user.getUserPassword(),getName()); 
        } catch(Exception e) {
        	log.info("用户名或密码错误");
        	setAttribute(MESSAGE, "用户名或密码错误");
        	return null;
        }
	}
	
	private void setAttribute(String key, String value) {
		SecurityUtils.getSubject().getSession().setAttribute(key, value);
	}
}
