# Genera arquitectura_diagrama.png para incrustar en el HTML/Word
Add-Type -AssemblyName System.Drawing
$w = 760
$h = 560
$bmp = New-Object System.Drawing.Bitmap $w, $h
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear([System.Drawing.Color]::FromArgb(255, 252, 252, 252))

$penLine = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(220, 55, 71, 79), 2)
$penDash = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(180, 100, 100, 100), 1.2)
$penDash.DashPattern = @(4.0, 3.0)
$penBox = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 33, 33, 33), 1.3)

$fontTitle = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
$fontBox = New-Object System.Drawing.Font("Segoe UI", 8.5)
$fontSmall = New-Object System.Drawing.Font("Segoe UI", 7.5)

$brushTitle = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(21, 101, 192))
$brushText = [System.Drawing.Brushes]::Black

function Draw-RoundedRect {
    param($Graphics, $X, $Y, $Width, $Height, $Radius)
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc($X, $Y, $Radius, $Radius, 180, 90)
    $path.AddArc($X + $Width - $Radius, $Y, $Radius, $Radius, 270, 90)
    $path.AddArc($X + $Width - $Radius, $Y + $Height - $Radius, $Radius, $Radius, 0, 90)
    $path.AddArc($X, $Y + $Height - $Radius, $Radius, $Radius, 90, 90)
    $path.CloseFigure()
    $Graphics.FillPath([System.Drawing.Brushes]::White, $path)
    $Graphics.DrawPath($script:penBox, $path)
    $path.Dispose()
}

function Draw-CenteredString {
    param($Graphics, $Text, $CenterX, $Top, $Font, $Brush)
    $sz = $Graphics.MeasureString($Text, $Font)
    $Graphics.DrawString($Text, $Font, $Brush, [float]($CenterX - $sz.Width / 2), [float]$Top)
}

Draw-CenteredString $g "COMPOSICION Y DATOS" ($w / 2) 12 $fontTitle $brushTitle
Draw-RoundedRect $g 140 36 480 40 8
Draw-CenteredString $g "TurismoApplication" ($w / 2) 48 $fontBox $brushText
Draw-CenteredString $g "EmpresaRepositoryImpl (EmpresaDao + EmpresaApiService)" ($w / 2) 64 $fontSmall $brushText

Draw-CenteredString $g "DOMINIO (contrato)" ($w / 2) 92 $fontTitle $brushTitle
Draw-RoundedRect $g 250 110 260 34 8
Draw-CenteredString $g "«interface» EmpresaRepository" ($w / 2) 122 $fontBox $brushText

Draw-CenteredString $g "PERSISTENCIA / RED" ($w / 2) 158 $fontTitle $brushTitle
Draw-RoundedRect $g 40 176 220 38 8
Draw-CenteredString $g "AppDatabase -> EmpresaDao (Room)" 150 192 $fontBox $brushText
Draw-RoundedRect $g 500 176 220 38 8
Draw-CenteredString $g "EmpresaApiService (Retrofit)" 610 192 $fontBox $brushText

$g.DrawLine($penLine, [int]($w / 2), 144, [int]($w / 2), 176)
$g.DrawLine($penLine, 320, 144, 150, 176)
$g.DrawLine($penLine, 440, 144, 610, 176)

Draw-CenteredString $g "VIEWMODEL" ($w / 2) 228 $fontTitle $brushTitle
Draw-RoundedRect $g 30 248 210 32 8
Draw-CenteredString $g "ListViewModel" 135 260 $fontBox $brushText
Draw-RoundedRect $g 275 248 210 32 8
Draw-CenteredString $g "DetailViewModel" 380 260 $fontBox $brushText
Draw-RoundedRect $g 520 248 210 32 8
Draw-CenteredString $g "MapViewModel" 625 260 $fontBox $brushText

$g.DrawLine($penDash, 135, 248, 320, 144)
$g.DrawLine($penDash, 380, 248, 380, 144)
$g.DrawLine($penDash, 625, 248, 440, 144)

Draw-CenteredString $g "VISTA (VIEW)" ($w / 2) 298 $fontTitle $brushTitle
Draw-RoundedRect $g 30 318 210 32 8
Draw-CenteredString $g "ListFragment" 135 330 $fontBox $brushText
Draw-RoundedRect $g 275 318 210 32 8
Draw-CenteredString $g "DetailFragment" 380 330 $fontBox $brushText
Draw-RoundedRect $g 520 318 210 32 8
Draw-CenteredString $g "MapFragment" 625 330 $fontBox $brushText

$g.DrawLine($penLine, 135, 318, 135, 280)
$g.DrawLine($penLine, 380, 318, 380, 280)
$g.DrawLine($penLine, 625, 318, 625, 280)

Draw-CenteredString $g "ACTIVITY Y NAVEGACION" ($w / 2) 368 $fontTitle $brushTitle
Draw-RoundedRect $g 180 388 400 38 8
Draw-CenteredString $g "MainActivity + NavHost (Navigation Component)" ($w / 2) 404 $fontBox $brushText
$g.DrawLine($penLine, [int]($w / 2), 388, [int]($w / 2), 350)

Draw-CenteredString $g "Adaptadores: EmpresaListAdapter, ActividadListAdapter" ($w / 2) 440 $fontSmall $brushText
Draw-CenteredString $g "Mappers DTO/Entity y modelos de dominio (Empresa, Contacto, ...)" ($w / 2) 458 $fontSmall $brushText

$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $dir "arquitectura_diagrama.png"
$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Host "Saved $out"
