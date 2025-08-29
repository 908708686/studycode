# postData 
$response = @{
    status = "success"
    message = ""
}

function Send-Mail {  
    param (  
        [string] $adname,
        [string] $code,
        [string] $emailaddress
    )  
    #定义邮件发送参数
    WriteLog "mmmmmmmmmmmmm"
    $SMTPServer = "smtp.exmail.qq.com" 
    #使用了加密端口 465端口如果发送不成功 可以尝试使用587端口 这里不介绍465和587端口的区别 有兴趣的可以自行百度查询
    #$SMTPPort = "465"
    $SMTPAccount = "xx@xx.cn"
    $SecurePassword =  ConvertTo-SecureString "xxxxxxx" -AsPlainText -Force
    $cred= New-Object System.Management.Automation.PSCredential($SMTPAccount, $SecurePassword)
    $EmailSubject = "xxxxx"
    #编写邮件正文，可以使用html编辑器进行编辑，需要注意的是HTML源代码一定要粘贴在@" "@中间
    $Emailbody = " $adname 您好!您的域账号验证码是：$code 您正在使用重置密码功能，验证码提供他人可能导致域账号被盗，请勿转发或泄漏。"
    #发送邮件
    Send-MailMessage -SmtpServer $SMTPServer  -From $SMTPAccount -To $emailaddress -Subject $EmailSubject -Bodyashtml -body $Emailbody -credential $cred -Encoding ([System.Text.Encoding]::UTF8)
}

# 检查AD中是否已存在用户 
Write-Host $postData.adname >> 'C:\adweb.log'
$temp_adname=$postData.adname
$adUser = Get-ADUser -Filter "SamAccountName -eq '$temp_adname' " -ErrorAction SilentlyContinue
WriteLog "adUser=$adUser"
if($adUser){
    # 随机生成6个数字  
    $code = (Get-Random -Minimum 100000 -Maximum 1000000).ToString().PadLeft(6, '0')
    # 缓存验证码
    Set-ADUser -Identity $postData.adname -Office $code
    $emailaddress=(Get-ADUser -Identity $postData.adname -Properties mail).mail
    WriteLog  "获取"$postData.adname + "的邮箱为:" +$emailaddress
    Send-Mail -adname $postData.adname -code $code -emailaddress $emailaddress
    $response.message =  "验证码已经发送到" + $emailaddress+"请注意查收！"
    Send-WebResponse $context (ConvertTo-Json $response)
}
else{
    WriteLog "无效的域账号" + $postData.adname
    $response.message = "无效的域账号"
    Send-WebResponse $context (ConvertTo-Json $response)
}