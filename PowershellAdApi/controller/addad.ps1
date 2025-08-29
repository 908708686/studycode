# postData 
$response = @{
    status  = "success"
    message = ""
}
#去json
$token = $postData.token
$adname = $postData.adname
$password = $postData.password | ConvertTo-SecureString -AsPlainText -Force
$mail = $postData.mail
$des = $postData.des
$path = $postData.path
# 检查AD中是否已存在用户
if ($token -eq $secret) {
    $adUser = Get-ADUser -Filter "SamAccountName -eq '$adname' " -ErrorAction SilentlyContinue
    WriteLog $adUser
    if ($adUser) {
        # 获取缓存验证码
        $response.message = "域账号已存在"
        Send-WebResponse $context (ConvertTo-Json $response)
    }
    else {
        try {
            New-ADUser -path $path -name $adname -SamAccountName $adname -userPrincipalName $pname -EmailAddress $mail -description $des -accountpassword $password -enabled $true -PasswordNeverExpires $true
            $response.message = "域账号" + $adname + "已建立成功"
        }
        catch {
            <#Do this if a terminating exception happens#>
            WriteLog "Exception: $_"
            $response.message = "Exception: $_"
        }
        Send-WebResponse $context (ConvertTo-Json $response)
    }
}




