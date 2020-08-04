Pod::Spec.new do |spec|

  # ―――  Spec Metadata  ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  These will help people to find your library, and whilst it
  #  can feel like a chore to fill in it's definitely to your advantage. The
  #  summary should be tweet-length, and the description more in depth.
  #

  spec.name         = "xnet"
  spec.version      = "0.0.1"
  spec.license       = { :type => 'MIT' }
  spec.summary      = "腾讯云xp2p框架"
  spec.description  = "高级版的点对点P2P(Peer-to-Peer)网络系统，起源于为视频直播、点播节省带宽"
  spec.homepage     = "https://github.com/tencentyun/xnet-ios-sdk"
  spec.author             = { "TencentCloud" => "XP2P" }
  spec.source       = { :git => "https://github.com/tencentyun/xnet-ios-sdk.git", :tag => spec.version.to_s }
  spec.prepare_command = <<-CMD
    curl -O https://xnet-ios-1255868781.cos.ap-guangzhou.myqcloud.com/e81b5e97a7cafd7882064bf9ab9fe50b581c113b/TencentXP2P.framework.zip
    unzip -o TencentXP2P.framework.zip
                   CMD
  spec.requires_arc = true
  spec.ios.deployment_target = "8.0"
  spec.source_files = "Classes/xnet/*.{h,m,mm,c,cc,cpp}"
  spec.vendored_frameworks = "TencentXP2P.framework"
  spec.frameworks = 'TencentXP2P'
  spec.libraries = 'c++', 'z'

end


