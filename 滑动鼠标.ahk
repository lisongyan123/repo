; ========== AutoHotkey Script Configuration ==========
; 使用 AutoHotkey v2.0+ 编写
#SingleInstance Force  ; 如果脚本已运行，则替换它。
SendMode "Input"       ; 提高 Send 命令的可靠性和速度。
SetWorkingDir A_ScriptDir  ; 设置脚本的工作目录。
CoordMode "ToolTip", "Screen"  ; 将工具提示坐标设置为相对于屏幕。

; ========== Site Configuration ==========
; 定义网站名称和对应的 URL
siteName1 := "Google"
siteName2 := "GitHub"
siteName3 := "YouTube"
siteName4 := "知乎"
siteName5 := "Bilibili"

siteUrl1 := "https://www.google.com"
siteUrl2 := "https://github.com"
siteUrl3 := "https://www.youtube.com"
siteUrl4 := "https://www.zhihu.com"
siteUrl5 := "https://www.bilibili.com"

; ========== Main Script Logic ==========
; 当按下鼠标中键 (MButton) 时触发
MButton::
{
    ; 构建菜单
    myMenu := Menu()
    myMenu.Add(siteName1, OpenUrl)
    myMenu.Add(siteName2, OpenUrl)
    myMenu.Add(siteName3, OpenUrl)
    myMenu.Add(siteName4, OpenUrl)
    myMenu.Add(siteName5, OpenUrl)
    
    ; 显示菜单在鼠标当前位置
    MouseGetPos(&mouseX, &mouseY)
    myMenu.Show(mouseX, mouseY)
    
    ; 菜单关闭后，清理菜单
    myMenu.Delete()
}
return

; ========== Functions ==========
; 统一处理菜单项点击事件的函数
OpenUrl(ItemName, ItemPos, MyMenu)
{
    ; 循环查找与菜单项名称匹配的 URL
    Loop 5
    {
        currentNameVar := "siteName" . A_Index
        if (%currentNameVar% == ItemName)
        {
            currentUrlVar := "siteUrl" . A_Index
            Run(%currentUrlVar%)  ; 打开URL
            break  ; 找到并执行后，退出循环
        }
    }
}
