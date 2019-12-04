package com.leyou.auth.web;

import com.leyou.common.utils.VerifyCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
public class VerifyCodeController {
    @GetMapping("vercode")
    public void code(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //创建验证码生成器实例取得生成图片和随机字符串
        VerifyCode vc = new VerifyCode();
        BufferedImage image = vc.getImage();
        String text = vc.getText();
        //随机字符串存入session中
        HttpSession session = req.getSession();
        session.setAttribute("index_code",text);
        //用流传输
        VerifyCode.output(image,resp.getOutputStream());
    }
}