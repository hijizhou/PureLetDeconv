function [Fa,Fs] = fft_wavefilters(M,wtype,match)
%FFT_WAVEFILTERS: Frequency response of the wavelet filters. 
% 	
%   [Fa,Fs] = fft_wavefilters(M,wtype) computes the 1D frequency response
%   of the scaling/wavelet filters for the analysis/synthesis filterbank. 
%
%   Input:
%   - M : length of the filters.
%   - wtype : wavelet filter (see the Matlab function 'wfilters'
%   to find all the available filters).
% 	
%   Output:
%   - Fa : frequency response of the analysis scaling filter (Fa(1,:)) and
%   the analysis wavelet filter (Fa(2,:)).
%   - Fs : frequency response of the synthesis scaling filter (Fs(1,:)) and
%   the synthesis wavelet filter (Fs(2,:)). 	
%
%   See also fft_wavedec, fft_waverec, wfilters.
% 
%   Authors: Florian Luisier and Thierry Blu, March 2007
%   Biomedical Imaging Group, EPFL, Lausanne, Switzerland.
%   This software is downloadable at http://bigwww.epfl.ch/
% 
%   References:
%   [1] T. Blu and M. Unser, "The fractional spline wavelet transform: 
%   definition and implementation," Proc. IEEE International Conference on 
%   Acoustics, Speech, and Signal Processing (ICASSP'2000), Istanbul,
%   Turkey, 5-9 June 2000, vol. I, pp. 512-515.
%
%   CONTACT: Thierry Blu (thierry.blu@m4x.org), The Chinese University of Hong Kong.


if(~exist('match','var'))
    match = 0;
end

if(match)
    [LO_D,HI_D,...
        LO_R,HI_R] = wfilters(wtype);
    nu          = 0:1/M:(1-1/M);
    z           = exp(-2*1i*pi*nu);
    lowa  = polyval(LO_D,z);
    higha = polyval(HI_D,z);
    lows  = polyval(fliplr(LO_R),z);
    highs = polyval(fliplr(HI_R),z);
    Fa    = [lowa;higha];
    Fs    = [lows;highs];
else
    if(strcmp(wtype(1:3),'bio') || strcmp(wtype(1:3),'rbi'))
        ortho = 0;
    else
        ortho = 1;
    end
    [LO_D,HI_D,...
        LO_R,HI_R] = wfilters(wtype);
    nu          = 0:1/M:(1-1/M);
    z           = exp(-2*1i*pi*nu);
    if(ortho)
        lowa  = polyval(LO_D,z);
        higha = z.*lowa;
        higha = conj([higha(M/2+(1:M/2)) higha(1:M/2)]);
        Fa    = [lowa;higha];
        Fs    = Fa;
    else
        lowa  = polyval(LO_D,z);
        higha = polyval(HI_D,z);
        lows  = polyval(fliplr(LO_R),z);
        highs = polyval(fliplr(HI_R),z);
        Fa    = [lowa;higha];
        Fs    = [lows;highs];
    end
end