# 안전지킴이 Android Application
## LG Embedded 자동차 모빌리티 부문

# :oncoming_automobile: UWB 기반 골목길 교통사고 안전 서비스앱 SAFEGUARD

<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=Kotlin&logoColor=white"/><img src="https://img.shields.io/badge/Node.js-5FA04E?style=flat&logo=Node.js&logoColor=white"/><img src="https://img.shields.io/badge/Android-34A853?style=flat&logo=Android&logoColor=white"/>


![SafeGuard](https://github.com/user-attachments/assets/6dd1f9f1-8b6e-4452-afeb-67d0a9893d82)


## 프로젝트 소개

- SAFEGUARD는 좁은 길을 지나는 보행자를 향해 사각지대에서 접근해오는 차량을 UWB 통신 기반으로 감지할 수 있음.
- 감지가 되었을 때, 보행자에게 알림 및 진동을 띄워 위험을 알릴 수 있음.

<br>

## 1. 개발 환경

- Front : Kotlin
- Back-end : Node.js
- 버전 관리 : Github
- 협업 툴 : Notion, Github 
- 서비스 배포 환경 : Android Studio
  
<br>

## 2. 채택한 개발 기술과 브랜치 전략

### Kotlin

- 코틀린 언어를 사용해 개발하여, 간결하고 안전한 코드 작성을 통해 유지보수성과 개발 생산성을 높임.
- 
    
### Coroutines

- 비동기 작업을 간단하게 처리하기 위해 코루틴을 사용함.
- 

### Android-Estimote-UWB-SDK

- Core Ultra Wideband(UWB) Jetpack 라이브러리를 활용하여 UWB 지원 Android 기기와 비콘을 검색, 연결하기 위해 사용함.
- 각각의 비콘과 Connect 및 Disconnect를 반복하며 각 비콘과의 거리를 측정하여 사용자의 위치를 특정하는 방식으로 사용함.
  - Connect 및 Disconnect를 반복한 이유는 Estimote Beacon과 휴대폰의 다중 연결이 지원되지 않기 때문 

### 브랜치 전략

- Git-flow 전략을 기반으로 main, dev 브랜치와 1/aos 보조 브랜치를 운용했습니다.
- main, dev, Feat 브랜치로 나누어 개발을 하였습니다.
    - **main** 브랜치는 배포 단계에서만 사용하는 브랜치입니다.
    - **dev** 브랜치는 개발 단계에서 git-flow의 master 역할을 하는 브랜치입니다.
    - **1/aos** 브랜치는 기능 단위로 독립적인 개발 환경을 위하여 사용하고 merge 후 각 브랜치를 삭제함.

<br>

## 3. 프로젝트 구조

```
│  AndroidManifest.xml 
├─java
│  └─com
│      └─uwb
│          └─safeguard
│              ├─config
│              │      ApplicationClass.kt
│              │      BaseActivity.kt
│              │      BaseFragment.kt
│              │      BaseResponse.kt
│              │      XAccessTokenInterceptor.kt
│              │      
│              ├─src
│              │  │  GpsService.kt
│              │  │  MainActivity.kt
│              │  │  MainActivityInterface.kt
│              │  │  MainActivityRetrofitInterface.kt
│              │  │  MainService.kt
│              │  │  SplashActivity.kt
│              │  │  SSEClient.kt
│              │  │  
│              │  └─model
│              │          CarRes.kt
│              │          CarResponse.kt
│              │          UserDeleteReq.kt
│              │          UserRes.kt
│              │          
│              └─util
│                      Alarm.kt
│                      CustomDialog.kt
│                      Foreground.kt
│                      
└─res
    ├─drawable
    │      ic_launcher_background.xml
    │      ic_launcher_foreground.xml
    │      ripple_btn.xml
    │      start_btn.xml
    │      
    ├─layout
    │      activity_main.xml
    │      activity_splash.xml
    │      dialog_alarm.xml 
```

<br>

## 4. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2024-09-10 ~ 2024-10-22
- UI 구현 : 2024-09-10 ~ 2022-09-12
- 기능 구현 : 2024-09-13 ~ 2024-10-22

<br>

### 작업 관리

- GitHub 1/aos 기능 브랜치에 커밋하며 진행 상황을 공유했습니다.
- 주간회의를 진행하며 작업 순서와 방향성에 대한 고민을 나누고 Notion에 기록하였습니다.

<br>

## 5. 페이지별 기능

### [초기화면]
- 서비스 접속 초기화면으로 splash 화면이 잠시 나온 뒤 메인 페이지가 나타납니다.

| 초기화면 |
|----------|
|![초기 화면](https://github.com/user-attachments/assets/4dc747e5-5997-4b28-a472-543d1d7b7aba)|

<br>

### [메인화면]

#### 1. START 버튼
- UWB 기능을 시작할 수 있는 START 버튼이 나타납니다
- 사용자는 START 버튼을 활성화 시켜 UWB 기능을 통해 비콘과 통신하여 차량을 감지하고 위험 알림을 받을 수 있습니다.

| START 버튼 |
|----------|
|![START 버튼](https://github.com/user-attachments/assets/3383ef94-47ff-4c3b-ad2a-cb5ff53dd321)|

<br>

#### 2. UWB 비콘 탐색
- 버튼을 클릭하면 스마트폰이 UWB 통신으로 근처 비콘을 탐색하기 시작합니다

| UWB 탐색 |
|----------|
|![탐색중](https://github.com/user-attachments/assets/f79e18bf-6410-4b56-9332-9ccaa66043b3)|

<br>

#### 3. 탐색 중 접근 차량 감지
- 탐색된 비콘과 접근하는 차량의 속도를 이용하여 제동거리를 구하고 제동거리가 30m 이내면 경고창을 띄웁니다. 
- 경고는 알림창과 함께 핸드폰에 진동을 울립니다.

| 감지 후 경고 |
|----------|
|![경고](https://github.com/user-attachments/assets/e934948d-8a28-4665-a712-f3dbec863a6b)|

<br>






