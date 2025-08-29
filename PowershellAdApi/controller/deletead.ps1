# postData 
$response = @{
    status  = "success"
    message = ""
}
#去json
$token = $postData.token
$adname = $postData.adname
if ($token -eq $secret) {
    # 检查AD中是否已存在用户
    $adUser = Get-ADUser -Filter "SamAccountName -eq '$adname' " -ErrorAction SilentlyContinue
    WriteLog $adUser
    if ($adUser) {
        # 目前先不删除账号，改为禁用
        Disable-ADAccount -Identity $adname
        $response.message = "域账号已禁用"
        Send-WebResponse $context (ConvertTo-Json $response)
    }
    else {
    
        WriteLog " $adname 域账号不存在"
        $response.message = " $adname 域账号不存在"
        Send-WebResponse $context (ConvertTo-Json $response)
    }
    
}

