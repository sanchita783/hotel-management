/* ── Inline QR Code encoder (no CDN needed) ── */
/* Based on QR Code generator, MIT License     */
var QRCode=(function(){
"use strict";
var a={},b=a;
// QR error correction and encoding - minimal implementation
function qr(text,opts){
  var o=opts||{};
  var ecl=o.errorCorrectionLevel||'M';
  var margin=o.margin!=null?o.margin:4;
  // Use qrcodegen library approach inline
  var segs=QrSegment.makeSegments(text);
  var ecMap={L:Ecc.LOW,M:Ecc.MEDIUM,Q:Ecc.QUARTILE,H:Ecc.HIGH};
  var ec=ecMap[ecl.toUpperCase()]||Ecc.MEDIUM;
  return QrCode.encodeSegments(segs,ec,1,40,-1,true);
}

// ── QrCode base ─────────────────────────────────────────────
function QrCode(version,errorCorrectionLevel,dataCodewords,mask){
  this.version=version;
  this.errorCorrectionLevel=errorCorrectionLevel;
  this.size=version*4+17;
  var sz=this.size;
  this.modules=[];
  this.isFunction=[];
  for(var i=0;i<sz;i++){this.modules.push([]);this.isFunction.push([]);for(var j=0;j<sz;j++){this.modules[i].push(false);this.isFunction[i].push(false);}}
  this.drawFunctionPatterns();
  var allCodewords=this.addEccAndInterleave(dataCodewords);
  this.drawCodewords(allCodewords);
  if(mask==-1){var minPenalty=Infinity;for(var m=0;m<8;m++){this.applyMask(m);var p=this.getPenaltyScore();if(p<minPenalty){minPenalty=p;mask=m;}this.applyMask(m);}}
  this.mask=mask;
  this.applyMask(mask);
  this.drawFormatBits(mask);
  this.isFunction=[];
}
QrCode.encodeSegments=function(segs,ecl,minVer,maxVer,mask,boostEcl){
  if(minVer==null)minVer=1;if(maxVer==null)maxVer=40;if(mask==null)mask=-1;if(boostEcl==null)boostEcl=true;
  var version,dataUsedBits;
  for(version=minVer;;version++){
    var dataCapacityBits=QrCode.getNumDataCodewords(version,ecl)*8;
    dataUsedBits=QrSegment.getTotalBits(segs,version);
    if(dataUsedBits<=dataCapacityBits)break;
    if(version>=maxVer)throw new Error("Data too long");
  }
  if(boostEcl){[Ecc.MEDIUM,Ecc.QUARTILE,Ecc.HIGH].forEach(function(e){if(dataUsedBits<=QrCode.getNumDataCodewords(version,e)*8)ecl=e;});}
  var bb=[];
  segs.forEach(function(seg){appendBits(seg.mode.modeBits,4,bb);appendBits(seg.numChars,seg.mode.numCharCountBits(version),bb);seg.data.forEach(function(b){bb.push(b);});});
  var dataCapacityBits=QrCode.getNumDataCodewords(version,ecl)*8;
  appendBits(0,Math.min(4,dataCapacityBits-bb.length),bb);
  appendBits(0,(8-bb.length%8)%8,bb);
  for(var padbyte=0xEC;bb.length<dataCapacityBits;padbyte^=0xEC^0x11)appendBits(padbyte,8,bb);
  var dataCodewords=[];
  for(var i=0;i<bb.length;i+=8){var byte=0;for(var j=0;j<8;j++)byte=(byte<<1)|(bb[i+j]?1:0);dataCodewords.push(byte);}
  return new QrCode(version,ecl,dataCodewords,mask);
};
QrCode.prototype.getModule=function(x,y){return 0<=x&&x<this.size&&0<=y&&y<this.size&&this.modules[y][x];};
QrCode.prototype.drawFunctionPatterns=function(){
  var sz=this.size;
  for(var i=0;i<sz;i++){this.setFunctionModule(6,i,i%2==0);this.setFunctionModule(i,6,i%2==0);}
  this.drawFinderPattern(3,3);this.drawFinderPattern(sz-4,3);this.drawFinderPattern(3,sz-4);
  var alignPatPos=this.getAlignmentPatternPositions();var numAlign=alignPatPos.length;
  for(var i=0;i<numAlign;i++)for(var j=0;j<numAlign;j++){if(!((i==0&&j==0)||(i==0&&j==numAlign-1)||(i==numAlign-1&&j==0)))this.drawAlignmentPattern(alignPatPos[i],alignPatPos[j]);}
  this.drawFormatBits(0);this.drawVersion();
};
QrCode.prototype.drawFormatBits=function(mask){
  var data=this.errorCorrectionLevel.formatBits<<3|mask;var rem=data;
  for(var i=0;i<10;i++)rem=(rem<<1)^((rem>>>9)*0x537);
  var bits=(data<<10|rem)^0x5412;
  for(var i=0;i<=5;i++)this.setFunctionModule(8,i,getBit(bits,i));
  this.setFunctionModule(8,7,getBit(bits,6));this.setFunctionModule(8,8,getBit(bits,7));this.setFunctionModule(7,8,getBit(bits,8));
  for(var i=9;i<15;i++)this.setFunctionModule(14-i,8,getBit(bits,i));
  for(var i=0;i<8;i++)this.setFunctionModule(this.size-1-i,8,getBit(bits,i));
  for(var i=8;i<15;i++)this.setFunctionModule(8,this.size-15+i,getBit(bits,i));
  this.setFunctionModule(8,this.size-8,true);
};
QrCode.prototype.drawVersion=function(){
  if(this.version<7)return;
  var rem=this.version;for(var i=0;i<12;i++)rem=(rem<<1)^((rem>>>11)*0x1F25);
  var bits=this.version<<12|rem;
  for(var i=0;i<18;i++){var a=this.size-11+i%3,b=Math.floor(i/3);this.setFunctionModule(a,b,getBit(bits,i));this.setFunctionModule(b,a,getBit(bits,i));}
};
QrCode.prototype.drawFinderPattern=function(x,y){
  for(var dy=-4;dy<=4;dy++)for(var dx=-4;dx<=4;dx++){var dist=Math.max(Math.abs(dx),Math.abs(dy));var xx=x+dx,yy=y+dy;if(0<=xx&&xx<this.size&&0<=yy&&yy<this.size)this.setFunctionModule(xx,yy,dist!=2&&dist!=4);}
};
QrCode.prototype.drawAlignmentPattern=function(x,y){for(var dy=-2;dy<=2;dy++)for(var dx=-2;dx<=2;dx++)this.setFunctionModule(x+dx,y+dy,Math.max(Math.abs(dx),Math.abs(dy))!=1);};
QrCode.prototype.setFunctionModule=function(x,y,isDark){this.modules[y][x]=isDark;this.isFunction[y][x]=true;};
QrCode.prototype.addEccAndInterleave=function(data){
  var ver=this.version,ecl=this.errorCorrectionLevel;
  var numBlocks=QrCode.NUM_ERROR_CORRECTION_BLOCKS[ecl.ordinal][ver];
  var blockEccLen=QrCode.ECC_CODEWORDS_PER_BLOCK[ecl.ordinal][ver];
  var rawCodewords=Math.floor(QrCode.getNumRawDataModules(ver)/8);
  var numShortBlocks=numBlocks-rawCodewords%numBlocks;
  var shortBlockLen=Math.floor(rawCodewords/numBlocks);
  var blocks=[],k=0;
  for(var i=0;i<numBlocks;i++){var datLen=shortBlockLen-blockEccLen+(i<numShortBlocks?0:1);blocks.push(data.slice(k,k+datLen));k+=datLen;}
  var rs=QrCode.reedSolomonComputeDivisor(blockEccLen);
  var result=[];var maxBlockLen=shortBlockLen-blockEccLen+1;
  for(var i=0;i<maxBlockLen;i++)blocks.forEach(function(b){if(i<b.length)result.push(b[i]);});
  blocks.forEach(function(b){var ecc=QrCode.reedSolomonComputeRemainder(b,rs);for(var i=0;i<ecc.length;i++)result.push(ecc[i]);});
  return result;
};
QrCode.prototype.drawCodewords=function(data){
  var i=0,sz=this.size;
  for(var right=sz-1;right>=1;right-=2){if(right==6)right=5;for(var vert=0;vert<sz;vert++){for(var j=0;j<2;j++){var x=right-j,upward=((right+1)&2)==0,y=upward?sz-1-vert:vert;if(!this.isFunction[y][x]&&i<data.length*8){this.modules[y][x]=getBit(data[i>>>3],7-(i&7));i++;}}}}
};
QrCode.prototype.applyMask=function(mask){
  var sz=this.size;
  for(var y=0;y<sz;y++)for(var x=0;x<sz;x++){var invert;switch(mask){case 0:invert=(x+y)%2==0;break;case 1:invert=y%2==0;break;case 2:invert=x%3==0;break;case 3:invert=(x+y)%3==0;break;case 4:invert=(Math.floor(x/3)+Math.floor(y/2))%2==0;break;case 5:invert=x*y%2+x*y%3==0;break;case 6:invert=(x*y%2+x*y%3)%2==0;break;case 7:invert=((x+y)%2+x*y%3)%2==0;break;default:throw new Error();}if(!this.isFunction[y][x]&&invert)this.modules[y][x]=!this.modules[y][x];}
};
QrCode.prototype.getPenaltyScore=function(){
  var result=0,sz=this.size;
  for(var y=0;y<sz;y++){var runColor=false,runX=0,runHistory=[0,0,0,0,0,0,0];for(var x=0;x<sz;x++){if(this.modules[y][x]==runColor){runX++;if(runX==5)result+=3;else if(runX>5)result++;}else{this.finderPenaltyAddHistory(runX,runHistory);if(!runColor)result+=this.finderPenaltyCountPatterns(runHistory)*40;runColor=this.modules[y][x];runX=1;}}result+=this.finderPenaltyTerminateAndCount(runColor,runX,runHistory)*40;}
  for(var x=0;x<sz;x++){var runColor=false,runY=0,runHistory=[0,0,0,0,0,0,0];for(var y=0;y<sz;y++){if(this.modules[y][x]==runColor){runY++;if(runY==5)result+=3;else if(runY>5)result++;}else{this.finderPenaltyAddHistory(runY,runHistory);if(!runColor)result+=this.finderPenaltyCountPatterns(runHistory)*40;runColor=this.modules[y][x];runY=1;}}result+=this.finderPenaltyTerminateAndCount(runColor,runY,runHistory)*40;}
  for(var y=0;y<sz-1;y++)for(var x=0;x<sz-1;x++){var c=this.modules[y][x];if(c==this.modules[y][x+1]&&c==this.modules[y+1][x]&&c==this.modules[y+1][x+1])result+=3;}
  var dark=0;this.modules.forEach(function(row){row.forEach(function(c){if(c)dark++;});});
  var total=sz*sz,k=Math.ceil(Math.abs(dark*20-total*10)/total)-1;result+=k*10;
  return result;
};
QrCode.prototype.getAlignmentPatternPositions=function(){
  var ver=this.version;if(ver==1)return[];
  var numAlign=Math.floor(ver/7)+2,step=ver==32?26:Math.ceil((ver*4+4)/(numAlign*2-2))*2;
  var result=[6];for(var pos=this.size-7;result.length<numAlign;pos-=step)result.splice(1,0,pos);
  return result;
};
QrCode.prototype.finderPenaltyCountPatterns=function(runHistory){
  var n=runHistory[1];var core=n>0&&runHistory[2]==n&&runHistory[3]==n*3&&runHistory[4]==n&&runHistory[5]==n;return(core&&runHistory[0]>=n*4&&runHistory[6]>=n?1:0)+(core&&runHistory[6]>=n*4&&runHistory[0]>=n?1:0);};
QrCode.prototype.finderPenaltyTerminateAndCount=function(currentRunColor,currentRunLength,runHistory){if(currentRunColor){this.finderPenaltyAddHistory(currentRunLength,runHistory);currentRunLength=0;}currentRunLength+=this.size;this.finderPenaltyAddHistory(currentRunLength,runHistory);return this.finderPenaltyCountPatterns(runHistory);};
QrCode.prototype.finderPenaltyAddHistory=function(currentRunLength,runHistory){if(runHistory[0]==0)currentRunLength+=this.size;runHistory.pop();runHistory.unshift(currentRunLength);};
QrCode.getNumRawDataModules=function(ver){var result=(16*ver+128)*ver+64;if(ver>=2){var numAlign=Math.floor(ver/7)+2;result-=(25*numAlign-10)*numAlign-55;if(ver>=7)result-=36;}return result;};
QrCode.getNumDataCodewords=function(ver,ecl){return Math.floor(QrCode.getNumRawDataModules(ver)/8)-QrCode.ECC_CODEWORDS_PER_BLOCK[ecl.ordinal][ver]*QrCode.NUM_ERROR_CORRECTION_BLOCKS[ecl.ordinal][ver];};
QrCode.reedSolomonComputeDivisor=function(degree){var result=new Array(degree).fill(0);result[degree-1]=1;var root=1;for(var i=0;i<degree;i++){for(var j=0;j<degree;j++){result[j]=QrCode.reedSolomonMultiply(result[j],root)^(j+1<degree?result[j+1]:0);}root=QrCode.reedSolomonMultiply(root,0x02);}return result;};
QrCode.reedSolomonComputeRemainder=function(data,divisor){var result=new Array(divisor.length).fill(0);data.forEach(function(b){var factor=b^result.shift();result.push(0);divisor.forEach(function(coef,i){result[i]^=QrCode.reedSolomonMultiply(coef,factor);});});return result;};
QrCode.reedSolomonMultiply=function(x,y){var z=0;for(var i=7;i>=0;i--){z=(z<<1)^((z>>>7)*0x11D);z^=((y>>>i)&1)*x;}return z;};
QrCode.ECC_CODEWORDS_PER_BLOCK=[
  [-1,7,10,15,20,26,18,20,24,30,18,20,24,26,30,22,24,28,30,28,28,28,28,30,30,26,28,30,30,30,30,30,30,30,30,30,30,30,30,30,30],
  [-1,10,16,26,18,24,16,18,22,22,26,30,22,22,24,24,28,28,26,26,26,26,28,28,28,28,28,28,28,28,28,28,28,28,28,28,28,28,28,28,28],
  [-1,13,22,18,26,18,24,18,22,20,24,28,26,24,20,30,24,28,28,26,30,28,30,30,30,30,28,30,30,30,30,30,30,30,30,30,30,30,30,30,30],
  [-1,17,28,22,16,22,28,26,26,24,28,24,28,22,24,24,30,28,28,26,28,30,24,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30,30],
];
QrCode.NUM_ERROR_CORRECTION_BLOCKS=[
  [-1,1,1,1,1,1,2,2,2,2,4,4,4,4,4,6,6,6,6,7,8,8,9,9,10,12,12,12,13,14,15,16,17,18,19,19,20,21,22,24,25],
  [-1,1,1,1,2,2,4,4,2,3,4,1,6,8,4,5,7,10,9,3,3,17,17,9,15,2,19,16,34,16,19,21,25,25,34,30,32,35,37,40,43],
  [-1,1,1,2,2,4,4,6,6,8,8,8,10,12,16,12,17,16,18,21,20,19,22,25,26,28,24,28,35,35,46,43,45,48,51,53,54,55,57,60,63],
  [-1,1,1,2,4,4,4,5,6,8,8,11,11,16,16,18,16,19,21,25,25,25,34,30,32,35,37,40,42,45,48,51,54,57,60,63,66,70,74,77,81],
];

// Ecc enum
function Ecc(ordinal,formatBits){this.ordinal=ordinal;this.formatBits=formatBits;}
Ecc.LOW=new Ecc(0,1);Ecc.MEDIUM=new Ecc(1,0);Ecc.QUARTILE=new Ecc(2,3);Ecc.HIGH=new Ecc(3,2);

// QrSegment
function QrSegment(mode,numChars,data){this.mode=mode;this.numChars=numChars;this.data=data;}
QrSegment.makeBytes=function(data){var bb=[];data.forEach(function(b){appendBits(b,8,bb);});return new QrSegment(QrSegment.Mode.BYTE,data.length,bb);};
QrSegment.makeNumeric=function(digits){if(!QrSegment.NUMERIC_REGEX.test(digits))throw new Error("Not numeric");var bb=[];for(var i=0;i<digits.length;){var rem=digits.length-i;if(rem>=3)appendBits(parseInt(digits.substr(i,3),10),10,bb);else if(rem==2)appendBits(parseInt(digits.substr(i,2),10),7,bb);else appendBits(parseInt(digits.charAt(i),10),4,bb);i+=rem>=3?3:rem;}return new QrSegment(QrSegment.Mode.NUMERIC,digits.length,bb);};
QrSegment.makeAlphanumeric=function(text){if(!QrSegment.ALPHANUMERIC_REGEX.test(text))throw new Error("Not alphanumeric");var bb=[];for(var i=0;i+2<=text.length;i+=2)appendBits(QrSegment.ALPHANUMERIC_CHARSET.indexOf(text.charAt(i))*45+QrSegment.ALPHANUMERIC_CHARSET.indexOf(text.charAt(i+1)),11,bb);if(text.length%2>0)appendBits(QrSegment.ALPHANUMERIC_CHARSET.indexOf(text.charAt(text.length-1)),6,bb);return new QrSegment(QrSegment.Mode.ALPHANUMERIC,text.length,bb);};
QrSegment.makeSegments=function(text){if(text=="")return[];else if(QrSegment.NUMERIC_REGEX.test(text))return[QrSegment.makeNumeric(text)];else if(QrSegment.ALPHANUMERIC_REGEX.test(text))return[QrSegment.makeAlphanumeric(text)];else return[QrSegment.makeBytes(toUtf8ByteArray(text))];};
QrSegment.getTotalBits=function(segs,version){var result=0;segs.forEach(function(seg){var ccbits=seg.mode.numCharCountBits(version);if(seg.numChars>=(1<<ccbits))return Infinity;result+=4+ccbits+seg.data.length;});return result;};
QrSegment.NUMERIC_REGEX=/^[0-9]*$/;
QrSegment.ALPHANUMERIC_REGEX=/^[A-Z0-9 $%*+.\/:-]*$/;
QrSegment.ALPHANUMERIC_CHARSET="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
function QrMode(modeBits,n1,n2,n3){this.modeBits=modeBits;this._numBitsCharCount=[n1,n2,n3];}
QrMode.prototype.numCharCountBits=function(ver){return this._numBitsCharCount[Math.floor((ver+7)/17)];};
QrSegment.Mode={NUMERIC:new QrMode(0x1,10,12,14),ALPHANUMERIC:new QrMode(0x2,9,11,13),BYTE:new QrMode(0x4,8,16,16),KANJI:new QrMode(0x8,8,10,12),ECI:new QrMode(0x7,0,0,0)};

function appendBits(val,len,bb){for(var i=len-1;i>=0;i--)bb.push((val>>>i)&1);}
function getBit(x,i){return((x>>>i)&1)!=0;}
function toUtf8ByteArray(str){var result=[];for(var i=0;i<str.length;){var c=str.codePointAt(i);if(c<0x80)result.push(c);else if(c<0x800){result.push(0xC0|(c>>6));result.push(0x80|(c&0x3F));}else if(c<0x10000){result.push(0xE0|(c>>12));result.push(0x80|((c>>6)&0x3F));result.push(0x80|(c&0x3F));}else{result.push(0xF0|(c>>18));result.push(0x80|((c>>12)&0x3F));result.push(0x80|((c>>6)&0x3F));result.push(0x80|(c&0x3F));}i+=c>=0x10000?2:1;}return result;}

// Public API: QRCode.toCanvas(canvas, text, opts, callback)
var QRCode={
  toCanvas:function(canvas,text,opts,cb){
    if(typeof opts==='function'){cb=opts;opts={};}
    opts=opts||{};
    try{
      var qr=QrCode.encodeSegments(QrSegment.makeSegments(text),Ecc.MEDIUM,1,40,-1,true);
      var sz=qr.size,margin=opts.margin!=null?opts.margin:4;
      var scale=Math.floor((opts.width||200)/(sz+margin*2))||1;
      var dim=(sz+margin*2)*scale;
      canvas.width=dim;canvas.height=dim;
      var ctx=canvas.getContext('2d');
      var dark=opts.color&&opts.color.dark||'#000000';
      var light=opts.color&&opts.color.light||'#ffffff';
      ctx.fillStyle=light;ctx.fillRect(0,0,dim,dim);
      ctx.fillStyle=dark;
      for(var y=0;y<sz;y++)for(var x=0;x<sz;x++)if(qr.getModule(x,y))ctx.fillRect((x+margin)*scale,(y+margin)*scale,scale,scale);
      if(cb)cb(null);
    }catch(e){if(cb)cb(e);}
  }
};
if(typeof window!=="undefined")window.QRCode=QRCode;