
<h1>개요</h1>
<p>
해당 프로젝트는 일구 형님의 자동차정비 차량 관리를 위한 프로그램 입니다.<br>
해당 프로그램은 오직 "고객 차량 관리"목적으로만 개발이 됩니다.<br>
<br>
해당 프로그램은  InteliJ IDEA  "Compose for Desktop"  을 이용하여 개발을 진행합니다.<br>
프로젝트 이름 : 19Garage <br>
개발툴 : IntelliJ IDEA <br>
사용 언어  : Kotlin Compose <br>
지원OS : Windows, Mac OS <br>
GitHub : https://github.com/zime78/19Garage  <br>
</p>
<br>

# DB 설계 <br>
![image](https://github.com/user-attachments/assets/17b565fc-aef8-4bdf-9f31-af64aa10add1)


---
<br>

<h1>참고 자료</h1>
<h3>빌드 방법</h3>

어플리케이션을 JVM 패키지로 빌드하려면:
```kotlin
./gradlew package
```

네이티브 실행 파일을 생성하려면:
```kotlin
./gradlew package -t native
```

이 명령어들은 각 플랫폼에 맞는 실행 파일을 빌드합니다. macOS, Windows, Linux 모두 지원됩니다.


<br><br>
### [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/#newProject)<br>
프로젝트 생성으로 해도 되므로 다른 용도로 사용하면 좋을듯함.
<p align="left">
<img width="50%" alt="image" src="https://github.com/user-attachments/assets/8601e31b-cff2-424a-ba56-15b4a7538aea">
</p>
<br>
<h3>UI Framework 참고</h3>
[Compose Desktop Components (KMP)](https://velog.io/@dkqk0124/Compose-Desktop-Components) <br>
[Kotlin Multiplatform Development](https://kotlinlang.org/docs/multiplatform-get-started.html) <br>
<br><br>
<h3> iOS + Android + Desktop</h3>
- [Getting started](https://jb.gg/start-cmp)
<br><br>
<h3>Desktop</h3>
- [Image and icon manipulations](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Image_And_Icons_Manipulations)<br>
- [Mouse events and hover](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Mouse_Events)<br>
- [Scrolling and scrollbars](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Desktop_Components#scrollbars)<br>
- [Tooltips](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Desktop_Components#tooltips)<br>
- [Context Menu](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Context_Menu/README.md)<br>
- [Top level windows management](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Window_API_new)<br>
- [Menu, tray, notifications](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Tray_Notifications_MenuBar_new)<br>
- [Keyboard support](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Keyboard)<br>
- [Tab focus navigation](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Tab_Navigation)<br>
- [Swing interoperability](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Swing_Integration)<br>
- [Navigation](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Navigation)<br>
- [Accessibility](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Accessibility)<br>
- [Building a native distribution](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution)<br>
- [UI testing](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html)<br>
<br><br>
<h3>Jetpack Compose 시작하기</h3>
Google Developers 개발 가이드 <br>
https://developer.android.com/develop/ui/compose/documentation?hl=ko <br>
<br><br>
<h1>블로그</h1>
[Compose for Desktop 마일스톤 1](https://blog.naver.com/PostView.nhn?blogId=tangunsoft&logNo=222270698048&parentCategoryNo=&categoryNo=27&viewDate=&isShowPopularPosts=false&from=postView)<br><br>
[Jetpack Compose for Desktop: 마일스톤 2 릴리스](https://blog.naver.com/PostView.naver?blogId=tangunsoft&logNo=222275867247&parentCategoryNo=&categoryNo=27&viewDate=&isShowPopularPosts=false&from=postView)<br>
<br><br>

<h1>샘플소스</h1>
샘플1 (이것이 최신 버전 샘플임)<br>
[compose-multiplatform-desktop-template](https://github.com/JetBrains/compose-multiplatform-desktop-template)
<br>
샘플 2<br>
[compose-multiplatform-desktop-template](https://github.com/JetBrains/compose-multiplatform-desktop-template)
<br>
샘플 3<br>
[compose-multiplatform](https://github.com/JetBrains/compose-multiplatform)
<br><br>


| Sample                                                                                                       | Description                                              | Platforms             |
| ------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------- | --------------------- |
| [Imageviewer](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/imageviewer)           | Image Viewer application                                 | Android, iOS, Desktop |
| [Codeviewer](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/codeviewer)             | File browser and code viewer application                 | Android, iOS, Desktop |
| [Chat](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/chat)                         | A simple chat                                            | Android, iOS, Desktop |
| [Graphics2D](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/graphics-2d)            | 2D Games and graphics examples                           | Android, iOS, Desktop |
| [Widgets Gallery](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/widgets-gallery)   | Gallery of standard widgets                              | Android, iOS, Desktop |
| [Todoapp Lite](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/todoapp-lite)         | A simple todo app fully based on Compose                 | Android, iOS, Desktop |
| [Issues tracker](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/issues)             | GitHub issue tracker with an adaptive UI and ktor-client | Android, Desktop      |
| [Notepad](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/notepad)                   | Notepad, using the Composable Window API                 | Desktop               |
| [IDEA plugin](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/intellij-plugin)       | Plugin for IDEA using Compose for Desktop                | Desktop               |
| [HTML based samples](https://github.com/JetBrains/compose-multiplatform/blob/master/examples/html/README.md) | Examples written with Compose HTML Library               |                       |



