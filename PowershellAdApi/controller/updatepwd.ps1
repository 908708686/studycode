

# postData

$response = @{
    status = "success"
    message = ""
}
#去json
$adname = $postData.adname
$oldpassword=$postData.oldpassword
$newpassword1= $postData.newpassword1

if($oldpassword -eq $newpassword1){
    $response.message='新密码和旧密码不能相同'
    Send-WebResponse $context (ConvertTo-Json $response)
    exit 1
}
$regex = '^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[\W_]).{8,}$'
if($newpassword1 -match $regex){
    $response.message='密码必须包含数字,字母,下划线，且大于8位'
    Send-WebResponse $context (ConvertTo-Json $response)
    exit 1
}

# 检查AD中是否已存在用户  
$adUser = Get-ADUser -Filter "SamAccountName -eq '$adname' " -ErrorAction SilentlyContinue
# 设置域名、用户名和密码
$password = ConvertTo-SecureString $oldpassword -AsPlainText -Force
# 创建凭据对象
if($adUser){
    WriteLog "经过检验域账号 $adname 存在" 
    $credential = New-Object System.Management.Automation.PSCredential($adname, $password)   
    try {
        # 使用凭据连接到域控制器并获取用户信息
        Get-ADUser -Identity $adname -Server $domain -Credential $credential | Out-Null
        WriteLog "经过检验，域账号 $adname 验证成功，账号密码正确" 
        Try{
            Set-ADAccountPassword -Identity $adname -NewPassword (ConvertTo-SecureString -AsPlainText $newpassword1 -Force)
        }
        Catch{
            $response.message=$adname + "密码不符合域的长度、复杂性"
            Send-WebResponse $context (ConvertTo-Json $response)
            exit 1
        }
        $response.message= $adname + "密码修改成功"
    } catch {
        $response.message= $adname +  "验证失败，账号或密码错误。"
        WriteLog  " $adname 验证失败，账号或密码错误。"
    }
}
else{
    $response.message = "无效的用户名或密码"
}
Send-WebResponse $context (ConvertTo-Json $response)