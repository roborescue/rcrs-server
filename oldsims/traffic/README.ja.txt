                  Morimoto Traffic Simulator ver.0.21
        for RoboCupRescue Prototype Simulation System ver.0.39

                            May. 11, 2002


本プログラムは，RoboCup2002 in Fukuoka/Busan の RoboCupRescue
Simulation League での使用を第一目的に開発された， RoboCupRescue
Prototype Simulation System 用交通シミュレータです．

本プログラムは Ver.0 で作られている既存のエージェントの便を考え，渥美交
通流シミュレータとの互換性を重視していますが，一部に，エージェント作成に
少なからぬ影響のあるものがあります．渥美交通流シミュレータで額面通りに扱
われていないものは本プログラムでも概ね扱いません．


I. Installation

To make this simulator, you simply type the following command:

    % make

    NOTE: JDK 1.3.0 is required


II. Usage

Synopsis is as follows:

    java traffic.Main [ hostname [ port ] ]

    hostname : kernel host name (default: localhost)
    port     : kernel port (default: 6000)


III. 実行環境

1/10 モデルのシミュレーションでは，計算負荷が大きいため，プログラムを少
なくとも3台のマシンに，以下のように振り分ける必要があります．

  ・救急隊/消防隊/道路啓開隊エージェント
  ・市民エージェント，火災シミュレータ
  ・カーネル，その他のシミュレータ (本プログラムを含む)

火災と交通は計算する時間帯が重なっており，分散させる必要があります．エー
ジェント群とサブシミュレータ群は計算する時間帯が異なるため，市民エージェ
ントと火災シミュレータを 1 台で計算しても問題ありません．

なお，本プログラムは以下の PC 3 台を使い動作を確認しました．

    CPU : Pentium III 930MHz
    MEM : 256MB
    OS  : Linux (Vine 2.1.5)
    JDK : 1.3.0


IV. エージェント開発に必要な最低限の情報

(1) 本プログラムは AK_MOVE/LOAD/UNLOAD を処理します． AK コマンドの仕様
は既存のシミュレータと同様です．

(1-1) 移動する (MovingObject のみ)
    Header: AK_MOVE
    Body:   routePlan : an array of IDs  (32 bit * number of route objects)
            0         : a sentinel of the array (32 bit)

移動経路 routePlan に沿って，エージェントを移動させます．移動経路は，現
在地を始点とする 1個以上の MotionlessObject の ID から構成される，下図の
ようなオートマトンが受理する正規言語の語です．

             n          n
          ------>    <------ OrgBldg
      Road       Node
          <------    ------> DestBldg
             r          b

       初期状態 | Road | Node | OrgBldg
      ----------+------+------+----------
       現在位置 | Road | Node | Building

       入力記号 | n                | r                | b
      ----------+------------------+------------------+----------------------
       意    味 | 隣接するNodeのID | 隣接するRoadのID | 隣接するBuildingのID

      終了状態 : 全ての状態

(1-2) 負傷者を救急車に乗せる (AmbulanceTeam のみ)
    Header: AK_LOAD
    Body:   target : an ID (32 bit)

負傷者 target を救急車に乗せます．負傷者は救急隊と同じ場所にいなければな
りません．ただし，道路とその端点は同じ位置とみなします．

(1-3) 負傷者を救急車から降ろす (AmbulanceTeam のみ)
    Header: AK_UNLOAD
    Body:   Nothing

救急車に乗せている負傷者を降ろします．降ろされた負傷者の位置は，救急隊と
同じになります．

(2) 通過の可否

移動経路に閉塞や渋滞がある場合，エージェントはその道路を通過できず，閉塞
や渋滞の手前で停止します．

(2-1) 閉塞

道路の閉塞も渥美交通流シミュレータと同様であり，以下のよう定義されます．

    lineWidth             := width / (linesToHead + linesToTail)
    road.blockedlines     := floor(road.block / road.lineWidth / 2 + 0.5)
    road.aliveLinesTo...  := max(0, road.linesTo... - road.blockedLines)
    road.isPassableToHead :- road.aliveLinesToHead >= 1
                          or movingObject.positionExtra < road.length / 2
    road.isPassableToTail :- road.aliveLinesToTail >= 1
                          or movingObject.positionExtra > road.length / 2

閉塞は道路の真中の 1 点に存在することにします．また，車線が複数ある場合
は，外側の車線から順に閉塞することにします． isPassableTo... が true で
ある状況とは，道路の内側に閉塞していない車線があるか，あるいは既に閉塞を
越えた位置にいる状況を意味します．

閉塞した道路の通過の可否は，車も人も同様に判定します．

(2-2) 渋滞

本プログラムは，エージェントを，前方の車との車間距離を安全に保ちつつ移動
させます．このため，車や人が密集すると渋滞が発生します．

前方の車までの最短の車間距離は MIN_SAFE_DISTANCE_BETWEEN_CARS，
前方の人までの最短の間隔は MIN_SAFE_DISTANCE_BETWEEN_CIVILIAN です
(cf. Constants.java)．

ただし，車 (緊急車) を優先して，市民は車にひかれないように注意深く移動す
ると考え，車は市民を無視して移動できます．また，前方に停止している車や人
がいる場合でも，別の車線が空いていれば車線を変更できるため，通過すること
ができます．

(3) その他

(3-1) 車の最高速度は MAX_VELOCITY_PER_SEC [mm/sec]
(cf. Constants.java) であり， 1 cycle で移動できる最長距離は高々 
MAX_VELOCITY_PER_SEC * 60 [mm] です．

(3-2) Ver.0では移動する車は緊急車両のみなので，信号, 右左折の制限は考え
ません．
    
(3-3) 渥美交通流シミュレータで使われていなかった中央分離帯，歩行者用道路
幅，実装されていなかった carsPassTo...， humansPassTo...は，本プログラム
でも扱いません．


V. より深いエージェント開発のために

(1) シミュレーションの概要

本プログラムの処理でキーとなるのは，道路に車線の概念が明確にあることです．
エージェントは，建物の中にいる場合をのぞいて，常に車線上にいます．本プロ
グラムがおこなう処理は，

  車線上を移動する
  建物に入る
  負傷者を乗せる/降ろす
  交差点を通過する
  閉塞を回避する
  停止車両を追い越す
  転回する
  目的地到着後に車を外側の車線に寄せる
  建物から路上に出る

であり，このうち下の 6 つの処理は，車線変更処理に帰着されます．
Prototype Simulation System の仕様上，エージェントには自分がどの車線上に
いるかを知る術はありませんが，渋滞の回避や解消のためには，車線を意識した
エージェントプログラミングが重要になると考えられます．この章では車線を中
心に，本プログラムが行うシミュレーションの概要を説明します．

(2) 車線上の移動

エージェントの移動は，車線上の移動と車線変更の繰り返しによって計算されま
す．つまり，障害物 (目的地や移動する車線上にいる前方の車両も含む) まで車
線上を直進し，障害物を越えるために車線を変更する，という処理を，目的地に
たどり着くまで繰り返します．

エージェントは障害物の手前で確実に停止できるように，速度を抑えて移動しま
す．速度は，現在の速度に加速度を加えることで決定し，加速度 a は以下の方
程式 (1) を a について解くことにより求めます．

    dx  : distance from the forward object
    ma  : maximum acceleration
    msd : minimum safe distance to forward object
    v   : velocity
    a   : acceleration
    safeDistance = (v + a)^2 / (2 * ma) + msd
    dx - safeDistance = v + a                  ... (1)

    NOTE: t = (v + a) / ma : 停止までにかかる時間
          (v + a) / 2 * t  : 停止までに移動する距離

車の加速度には制限があり，車は停止状態から最高速度になるまで (その逆も同
様) に，少なくとも ACCELERATING_SEC [sec] (cf. Constants.java) を要しま
す．市民は歩いて移動するため，加速度に制限はありません．

各 cycle の初速には，前の cycle の最終速度を用います．ただし，新しい
cycle で与えられたエージェントの行動内容が，移動でなかったり，逆方向への
移動である場合には，急停止したと考え，初速を 0 [mm/sec] にします．これは
シミュレーションの時間粒度 1 分が粗過ぎるためやむを得ません．

(3) 車線の変更

先に述べたように，以下の行為

  交差点を通過する
  閉塞を回避する
  停止車両を追い越す
  転回する
  目的地到着後に車を外側の車線に寄せる
  建物から路上に出る

は車線変更として処理されます．

車線変更の処理では，変更先の候補の車線群の各車線毎に，割り込みの可否を判
定し，割り込み可能なら車線を変更します．割り込みが可能な状況とは，割り込
んでも車線の前方と後方にいる車との車間距離が安全に保たれる状況です．変更
候補のどの車線にも割り込めない場合は，割り込めるまで待機します．

より大きなあるいは同等の道路 (進行方向の車線数が多い，または等しい道路 
--- つまり，優先道路) と交わる Node (分岐, 合流を含む) を交差点と呼びま
す．

(4) 移動経路とその処理

シミュレーションの処理内容の理解を助けるために，移動経路に応じた処理内容
のイメージを図を用いて述べます．移動経路は以下の表の記号によって表現しま
す．

   B        | N    | R
  ----------+------+------
   Building | Node | Road

処理内容のイメージは以下の表の記号によって表現します．

   [ ]      | :    | ---> | +        | s          | m
  ----------+------+------+----------+------------+--------------------
   Building | Node | Lane | Blockade | self agent | other MovingObject

  case 1:
    route plan: {B}
      # 何もしない
      [s]        [s]
      before     after

  case 2:
    route plan: {... R}
    又は AK_MOVE は送られてないが，Road 上にいる
      # 他の車の妨げにならないように，一番外側の車線に寄る
      ------->       ---s--->
      ---s--->       ------->
      <-------       <-------
      <-------       <-------
       before         after

  case 3:
    route plan: {... N}
    又は AK_MOVE は送られてないが，Node 上にいる
      # 他の車の妨げにならないように，一番外側の車線に寄る
      ------->:       ------->s
      ------->s       ------->:
      <-------:       <-------:
      <-------:       <-------:
       before           after

  case 4:
    route plan: {B N}
      # N から出ている車線群に車線変更する
             [s]                   [ ]
              |                     |
      ------->:------->      ------->s------->
      <-------:<-------      <-------:<-------
           before                  after

  case 5:
    route plan: {B N B}
      # Nから出ている車線群に車線変更した後，目的の建物に入る
             [s]                    [ ]                    [ ]
              |                      |                      |
      ------->:------->      ------->s------->      ------->:-------->
      <-------:<-------      <-------:<-------      <-------:<--------
              |                      |                      |
             [ ]                    [ ]                    [s]
           before                  after                more after

  case 6:
    route plan: {B N R} ({B R} is similar)
      # N から出ている R上の車線群の始点に車線変更する
      [s]             [ ]
       |               |
       :------->       :s------>
       :<-------       :<-------
         before           after

  case 7:
    route plan: {... N B}
      # B に入る
             [ ]            [s]
              |              |
      ------->s      ------->:
      <-------:      <-------:
        before         after

  case 8:
    route plan: {... N R ...}
      case 8-1: ... -> N -> R -> ...
        case 8-1-1: N から R に直進可能
          # 進む
          s------->       :---s--->
          before           after

        case 8-1-2: N が R への交差点
          # N から出ている R 上の車線群の始点に車線変更した後，進む
           :------->       :s------>       :---s--->
          s:               :               :
            before           after         more after

        case 8-2:      ->    -> N  -> ...
                  ... <-  R <-    <-
        # N から出ている R 上の車線群の始点に車線変更した後，進む
        ------->s------->       ------->:------->       ------->:------->
        <-------:               <------s:               <---s---:
             before                    after                more after

  case 9:
    route plan: {... R N ...}
      case 9-1: ... -> R -> N -> ...
        case 9-1-1: R から N に直進可能
        # 進む
        ---s--->:       ------->s
         before           after

        case 9-1-2: R が 閉塞している
          case 9-1-2-1: 既に閉塞 (道路の中心) を過ぎた位置にいる
          # 進む
          ---+s-->:       ---+--->s:
           before           after

          case 9-1-2-2: 閉塞の手前にいる
          # 前進できる車線群に車線変更した後，進む (なければそこで停止)
          --s+--->:       ---+--->:       ---+---> :
          ------->:       --s---->:       ------->s:
           before           after         more after

        case 9-1-3: 移動物体の手前にいる
        # 前進できる車線群に車線変更した後，進む (なければそこで停止)
        -s---m->:       -----m->:       -----m-> :
        ------->:       -s----->:       ------->s:
         before           after         more after

      case 9-2:      ->    -> R  -> ...
                ... <-  N <-    <-
      # N に向かう R上の反対車線群の同じ位置に車線変更した後，進む (== 転回)
      :-------s->       :--------->       : --------->
      :<---------       :<------s--       :s<---------
         before            after           more after

(5) AK_MOVE/LOAD/UNLOAD 処理優先度

AK_MOVE を最優先で処理します．AK_LOAD/UNLOAD は，ターゲットの負傷者が 
AK_MOVE を送信しなかった場合のみ行います．

Prototype Simulation System Ver.0.31 以前は，AK_LOAD/UNLOAD はMisc
Simulator が処理していましたが，Simulation System Ver.0.36 では，
position, positionExtra, positionHistory プロパティの競合を防ぐため，交
通シミュレータが処理します．


VI. 本プログラムの開発/保守に必要な情報

(1) エージェントの位置に関する以下のプロパティ

  MovingObject.position
  MovingObject.positionExtra
  MovingObject.positionHistory

は，交通シミュレータのみが決定し，他のシミュレータが変更しないことを前提
とします．これが保証されないと，シミュレータ外部の状態と，本プログラムが
内部に保っているエージェントの状態 (車線や速度など) との間に一貫性がなく
なり，シミュレーションのプログラミングが困難になります．

positionHistory は渥美交通流シミュレータと同様であり，経路上の Node のリ
ストとして表現されます．

(2) Constants.java で定義されている定数値の変更により，設定が変更できま
す．

  シミュレーションの計算単位時間	UNIT_SEC
  車/人の速度				MAX_VELOCITY_PER_SEC
					MAX_CIV_VELOCITY_PER_SEC
  車の最高速度までにかかる時間		ACCELERATING_SEC
  車/人の停止時の安全な車間距離		MIN_SAFE_DISTANCE_BETWEEN_CARS
					MIN_SAFE_DISTANCE_BETWEEN_CIVILIAN
  進行方向				DRIVING_DIRECTION_IS_LEFT
  計算を打ち切る時間 (cf. VI (3-3))	CALCULATING_LIMIT_MILLI_SEC

(3) シミュレーションの流れ

シミュレーションは，まず，
  traffic.Simulator.Simulator(InetAddress kernelAddress, int kernelPort)
により初期化を行い，以後，
  traffic.Simulator.simulate()
を繰り返すことで進行します．

(3-1) traffic.Simulator.simulate() の概要

  エージェントの行動内容を受信	traffic.io.receiveCommands()
  AK_MOVE を処理		traffic.Simulator.move()
  AK_LOAD/UNLOAD を処理		traffic.loadUnload()
  シミュレーション結果を送信	traffic.io.sendUpdate()
  シミュレーション結果を受信	traffic.io.receiveUpdate()

(3-2) エージェントの行動内容のチェック

エージェントの行動内容受信時に，
  traffic.WorldModel.parseCommands(int[] data)
により，行動内容の解釈と妥当性のチェックを行います．行動内容が仕様を満た
していない場合は，この段階ではじきます．

(3-3) AK_MOVE の処理の概要

各サイクルの最初に，
  traffic.object.MovingObject.initializeEveryCycle()
により，移動経路等を初期化し，UNIT_SEC (cf. VI (3-2)) 単位でエージェント
の移動をシミュレートします．1 単位時間毎のシミュレーションの概要は以下の
とおりです．

  1) シミュレーションの打ち切り判定
    CALCULATING_LIMIT_MILLI_SEC

  2) 移動する車線の列と障害物を調べる
    traffic.object.MovingObject.setMotionlessObstructionAndMovingLaneList()

  3) 移動車線列上にいる前方車両を調べる
    traffic.object.MovingObject.setMovingObstruction()

  4) 障害物の種類に応じてエージェントを仕分けする
    traffic.Simulator.sortByObstruction()

  5) 前方車両に行く手を阻まれているエージェントを移動させる
    traffic.Simulator.m_waitingNoChangeList
    traffic.Simulator.moveBeforeForwardMvObj(MovingObject MovingObject mv)

  6) 閉塞，車線変更，転回の手前にいるエージェントを移動させる
    traffic.Simulator.m_waitingMap
    traffic.Simulator.dispatch(ArrayList follows)

  7) 建物の入口か目的地の手前にいるエージェントを移動させる
    traffic.Simulator.m_noWaitingList
    traffic.object.MovingObject.move()

  8) 障害物がないエージェントを移動させる
    traffic.Simulator.m_noWaitingNoChageList
    traffic.object.MovingObject.move()

1) は，シミュレーション結果を，毎サイクル，確実にカーネルに伝えるための，
やむを得ぬ処置です．最初の数ターンと，多数のエージェントが長距離移動する
ような場合に，計算を打ち切ることがあります．

6) の traffic.Simulator.dispatch(ArrayList follows) で車線変更するエージェ
ントの数は，1 つの障害物につき，1 単位時間当たり 1 エージェントに限って
います．


VII. License Of Morimoto Traffic Simulator

(1) Neither the RoboCupRescue committee nor development staffs of this
program provide warranty.  Use the software at your own risk.

(2) Copyright of all program code and documentation included in source
or binary package of this program belongs to Takeshi Morimoto.

(3) You can use this program for research and/or education purpose
only, commercial use is not allowed.


VIII. Author

Takeshi MORIMOTO
Ikuo Takeuchi Laboratory
Department of Computer Science
The University of Electro-Communications

Additional information can be found in:
    http://ne.cs.uec.ac.jp/~morimoto/rescue/traffic/

Mail bug reports and suggestions to:
    morimoto@takopen.cs.uec.ac.jp
