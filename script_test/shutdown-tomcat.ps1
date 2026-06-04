$client = New-Object System.Net.Sockets.TcpClient("127.0.0.1",8005)
$stream = $client.GetStream()

$writer = New-Object System.IO.StreamWriter($stream)
$writer.AutoFlush = $true
$writer.WriteLine("SHUTDOWN")

$writer.Dispose()
$stream.Dispose()
$client.Dispose()