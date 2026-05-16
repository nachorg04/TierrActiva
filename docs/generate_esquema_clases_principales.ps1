# Genera un diagrama PNG de relaciones principales (MVVM).
Add-Type -AssemblyName System.Drawing

$width = 1600
$height = 1100
$bitmap = New-Object System.Drawing.Bitmap $width, $height
$g = [System.Drawing.Graphics]::FromImage($bitmap)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear([System.Drawing.Color]::FromArgb(255, 248, 250, 252))

$fontTitle = New-Object System.Drawing.Font("Segoe UI", 24, [System.Drawing.FontStyle]::Bold)
$fontLayer = New-Object System.Drawing.Font("Segoe UI", 14, [System.Drawing.FontStyle]::Bold)
$fontBoxMain = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
$fontBoxSub = New-Object System.Drawing.Font("Segoe UI", 10)
$fontLegend = New-Object System.Drawing.Font("Segoe UI", 9)

$brushTitle = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(35, 56, 98))
$brushLayer = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(26, 89, 173))
$brushText = [System.Drawing.Brushes]::Black
$brushWhite = [System.Drawing.Brushes]::White

$penBox = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(40, 56, 78), 2)
$penSoft = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(160, 179, 200), 1.5)
$penSolidArrow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(50, 65, 85), 2.2)
$penDashedArrow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(90, 90, 90), 2)
$penDashedArrow.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
$penDashedArrow.DashPattern = @(5.0, 4.0)

$arrowCap = New-Object System.Drawing.Drawing2D.AdjustableArrowCap(6, 6, $true)
$penSolidArrow.CustomEndCap = $arrowCap
$penDashedArrow.CustomEndCap = $arrowCap

function New-RoundPath {
    param(
        [int]$X, [int]$Y, [int]$W, [int]$H, [int]$R
    )
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc($X, $Y, $R, $R, 180, 90)
    $path.AddArc($X + $W - $R, $Y, $R, $R, 270, 90)
    $path.AddArc($X + $W - $R, $Y + $H - $R, $R, $R, 0, 90)
    $path.AddArc($X, $Y + $H - $R, $R, $R, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-Card {
    param(
        [int]$X, [int]$Y, [int]$W, [int]$H,
        [string]$MainText, [string]$SubText = ""
    )
    $path = New-RoundPath -X $X -Y $Y -W $W -H $H -R 14
    $g.FillPath($brushWhite, $path)
    $g.DrawPath($penBox, $path)
    $path.Dispose()

    $mainRect = New-Object System.Drawing.RectangleF([float]($X + 12), [float]($Y + 10), [float]($W - 24), [float](($H / 2) - 6))
    $subRect = New-Object System.Drawing.RectangleF([float]($X + 12), [float]($Y + ($H / 2) - 2), [float]($W - 24), [float](($H / 2) - 12))

    $fmtCenter = New-Object System.Drawing.StringFormat
    $fmtCenter.Alignment = [System.Drawing.StringAlignment]::Center
    $fmtCenter.LineAlignment = [System.Drawing.StringAlignment]::Center

    $g.DrawString($MainText, $fontBoxMain, $brushText, $mainRect, $fmtCenter)
    if ($SubText -ne "") {
        $g.DrawString($SubText, $fontBoxSub, $brushText, $subRect, $fmtCenter)
    }
}

function Draw-Arrow {
    param([int]$X1, [int]$Y1, [int]$X2, [int]$Y2, [bool]$Dashed = $false)
    if ($Dashed) {
        $g.DrawLine($penDashedArrow, $X1, $Y1, $X2, $Y2)
    } else {
        $g.DrawLine($penSolidArrow, $X1, $Y1, $X2, $Y2)
    }
}

function Draw-Label {
    param([string]$Text, [int]$X, [int]$Y)
    $g.DrawString($Text, $fontLegend, $brushText, [float]$X, [float]$Y)
}

# Title
$g.DrawString("Esquema de relaciones entre clases principales", $fontTitle, $brushTitle, 360, 20)
$g.DrawString("Proyecto: Empresas Turismo Activo (MVVM)", (New-Object System.Drawing.Font("Segoe UI", 12)), $brushTitle, 520, 62)

# Layer guides
$g.DrawLine($penSoft, 80, 150, 1520, 150)
$g.DrawLine($penSoft, 80, 300, 1520, 300)
$g.DrawLine($penSoft, 80, 510, 1520, 510)
$g.DrawLine($penSoft, 80, 700, 1520, 700)
$g.DrawLine($penSoft, 80, 890, 1520, 890)

$g.DrawString("COMPOSICION", $fontLayer, $brushLayer, 90, 116)
$g.DrawString("DATOS (modelo + repositorio + fuentes)", $fontLayer, $brushLayer, 90, 266)
$g.DrawString("DATOS (persistencia / red)", $fontLayer, $brushLayer, 90, 476)
$g.DrawString("PRESENTACION (VIEWMODEL + VIEW)", $fontLayer, $brushLayer, 90, 666)
$g.DrawString("NAVEGACION / HOST", $fontLayer, $brushLayer, 90, 856)

# Boxes
Draw-Card -X 560 -Y 170 -W 480 -H 96 -MainText "TurismoApplication" -SubText "Crea AppDatabase, EmpresaApiService y EmpresaRepositoryImpl"

Draw-Card -X 640 -Y 330 -W 320 -H 86 -MainText "EmpresaRepository" -SubText "Interfaz (data.repository)"

Draw-Card -X 640 -Y 540 -W 320 -H 96 -MainText "EmpresaRepositoryImpl" -SubText "Implementacion (data.repository)"
Draw-Card -X 180 -Y 540 -W 360 -H 96 -MainText "AppDatabase + EmpresaDao" -SubText "Room (data.local)"
Draw-Card -X 1060 -Y 540 -W 360 -H 96 -MainText "EmpresaApiService" -SubText "Retrofit (data.remote)"

Draw-Card -X 140 -Y 730 -W 300 -H 92 -MainText "ListViewModel" -SubText "ui.list"
Draw-Card -X 650 -Y 730 -W 300 -H 92 -MainText "DetailViewModel" -SubText "ui.detail"
Draw-Card -X 1160 -Y 730 -W 300 -H 92 -MainText "MapViewModel" -SubText "ui.map"

Draw-Card -X 140 -Y 850 -W 300 -H 92 -MainText "ListFragment" -SubText "Observa ListViewModel"
Draw-Card -X 650 -Y 850 -W 300 -H 92 -MainText "DetailFragment" -SubText "Observa DetailViewModel"
Draw-Card -X 1160 -Y 850 -W 300 -H 92 -MainText "MapFragment" -SubText "Observa MapViewModel"

Draw-Card -X 560 -Y 960 -W 480 -H 92 -MainText "MainActivity + NavHostFragment" -SubText "Contenedor de navegacion y destinos"

Draw-Card -X 20 -Y 930 -W 260 -H 92 -MainText "Adapters UI" -SubText "EmpresaListAdapter / ActividadListAdapter"

# Arrows
Draw-Arrow -X1 800 -Y1 266 -X2 800 -Y2 330 -Dashed:$false                    # Application -> interface
Draw-Arrow -X1 800 -Y1 416 -X2 800 -Y2 540 -Dashed:$true                     # Interface <- impl (implements)
Draw-Label -Text "implementa" -X 815 -Y 475

Draw-Arrow -X1 640 -Y1 588 -X2 540 -Y2 588 -Dashed:$false                    # RepoImpl -> Dao
Draw-Arrow -X1 960 -Y1 588 -X2 1060 -Y2 588 -Dashed:$false                   # RepoImpl -> Api
Draw-Label -Text "usa" -X 585 -Y 566
Draw-Label -Text "usa" -X 1005 -Y 566

Draw-Arrow -X1 290 -Y1 730 -X2 740 -Y2 416 -Dashed:$true                     # ListVM -> interface
Draw-Arrow -X1 800 -Y1 730 -X2 800 -Y2 416 -Dashed:$true                     # DetailVM -> interface
Draw-Arrow -X1 1310 -Y1 730 -X2 860 -Y2 416 -Dashed:$true                    # MapVM -> interface
Draw-Label -Text "depende de interfaz" -X 930 -Y 670

Draw-Arrow -X1 290 -Y1 850 -X2 290 -Y2 822 -Dashed:$false                    # ListFragment -> ListVM
Draw-Arrow -X1 800 -Y1 850 -X2 800 -Y2 822 -Dashed:$false                    # DetailFragment -> DetailVM
Draw-Arrow -X1 1310 -Y1 850 -X2 1310 -Y2 822 -Dashed:$false                  # MapFragment -> MapVM

Draw-Arrow -X1 800 -Y1 960 -X2 290 -Y2 942 -Dashed:$false                    # MainActivity hosts fragments
Draw-Arrow -X1 800 -Y1 960 -X2 800 -Y2 942 -Dashed:$false
Draw-Arrow -X1 800 -Y1 960 -X2 1310 -Y2 942 -Dashed:$false
Draw-Label -Text "host / navegacion" -X 820 -Y 948

Draw-Arrow -X1 280 -Y1 965 -X2 140 -Y2 890 -Dashed:$true                     # adapters -> list fragment
Draw-Arrow -X1 280 -Y1 980 -X2 650 -Y2 890 -Dashed:$true                     # adapters -> detail fragment
Draw-Label -Text "bind de listas" -X 285 -Y 940

# Legend
$g.DrawString("Leyenda:", $fontLayer, $brushLayer, 1220, 120)
$g.DrawLine($penSolidArrow, 1220, 155, 1320, 155)
$g.DrawString("Relacion directa (usa / observa / host)", $fontLegend, $brushText, 1330, 148)
$g.DrawLine($penDashedArrow, 1220, 180, 1320, 180)
$g.DrawString("Dependencia por contrato o implementacion", $fontLegend, $brushText, 1330, 173)

$outDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$outPath = Join-Path $outDir "esquema_clases_principales.png"
$bitmap.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bitmap.Dispose()
$fontTitle.Dispose()
$fontLayer.Dispose()
$fontBoxMain.Dispose()
$fontBoxSub.Dispose()
$fontLegend.Dispose()
$penBox.Dispose()
$penSoft.Dispose()
$penSolidArrow.Dispose()
$penDashedArrow.Dispose()
$arrowCap.Dispose()

Write-Host "Saved $outPath"
