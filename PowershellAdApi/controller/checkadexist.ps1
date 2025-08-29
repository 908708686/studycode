# postData
$response = @{
    status = "success"
    message = ""
}
#去json
$token = $postData.token
$adname = $postData.adname
WriteLog $time
if($token -eq $secret ){
  WriteLog "tt"
  $result= net user $adname
  WriteLog $result
  $response.message = $result
}else{
  $response.message = "无效的用户名或密码"
}
Send-WebResponse $context (ConvertTo-Json $response)