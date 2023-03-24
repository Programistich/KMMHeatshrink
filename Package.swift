// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KMMHeatshrink",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "KMMHeatshrink",
            targets: ["KMMHeatshrink"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "KMMHeatshrink",
            path: "./KMMHeatshrink.xcframework"
        ),
    ]
)
