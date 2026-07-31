import React from "react";

export function SpiderLogo({ size = 24, className = "" }: { size?: number, className?: string }) {
  return (
    <svg 
      className={className} 
      height={size} 
      width={size} 
      viewBox="0 0 100 100" 
      xmlns="http://www.w3.org/2000/svg"
    >
      <style>{`
        @keyframes glow-pulse {
          from { filter: drop-shadow(0 0 2px #ef4444); }
          to { filter: drop-shadow(0 0 6px #ef4444); }
        }
        .spider-head-glow {
          fill: #ef4444;
          animation: glow-pulse 1s infinite alternate;
        }
      `}</style>
      <path d="M50 45 C 38 45, 32 65, 50 85 C 68 65, 62 45, 50 45 Z" fill="currentColor"></path>
      <circle className="spider-head-glow" cx="50" cy="35" r="7"></circle>
      <path d="M46 31 Q 45 24 48 22" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="1.5"></path>
      <path d="M54 31 Q 55 24 52 22" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="1.5"></path>
      <path d="M46 45 Q 20 25 25 65" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M44 50 Q 5 35 20 80" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M44 55 Q 5 65 25 90" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M46 60 Q 20 80 35 95" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M54 45 Q 80 25 75 65" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M56 50 Q 95 35 80 80" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M56 55 Q 95 65 75 90" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
      <path d="M54 60 Q 80 80 65 95" fill="none" stroke="currentColor" strokeLinecap="round" strokeWidth="2"></path>
    </svg>
  );
}
