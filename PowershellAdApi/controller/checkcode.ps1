# postData 

$response = @{
    status = "success"
    message = ""
}
function Generate-RandomString() {  
    $characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'  
    $random = New-Object Random  
    $result = New-Object Text.StringBuilder  
    for ($i = 0; $i -lt 16; $i++) {  
        $result.Append($characters[$random.Next($characters.Length)]) | Out-Null
    }  
    return $result.ToString()
}  

# 检查AD中是否已存在用户  
$temp_adname=$postData.adname
$adUser = Get-ADUser -Filter "SamAccountName -eq '$temp_adname' " -ErrorAction SilentlyContinue
WriteLog "adUser=$adUser"
if($adUser){
    # 获取缓存验证码
    $tmpcode=(Get-ADUser -Identity $adUser -Properties Office).Office
    if($postData.code -eq $tmpcode ){
        $newPassword = Generate-RandomString 
        $newPassword = "xxxx" + $newPassword
        #设置新密码
        #Set-ADAccountPassword -Identity $username -NewPassword $newPassword
        Set-ADAccountPassword -Identity $postData.adname  -NewPassword (ConvertTo-SecureString -AsPlainText $newPassword -Force)
        $response.message = "新密码为" + $newPassword
        Send-WebResponse $context (ConvertTo-Json $response)
    }
    else{
        $response.message = "验证码错误"
        Send-WebResponse $context (ConvertTo-Json $response)
    }
}
else{
    WriteLog "无效的域账号" + $postData.adname
    $response.message = "无效的域账号"
    Send-WebResponse $context (ConvertTo-Json $response)
}

