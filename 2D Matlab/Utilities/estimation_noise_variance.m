function est_noise_variance = estimation_noise_variance (y)
% USAGE    : est_noise_variance = estimation_noise_variance (y)
% FUNCTION : Estimate the noise variance of an image y, by evaluating 
%            the Median of its Absolute Difference
%
% DATE     : 28 July 2015
% AUTHOR   : Thierry Blu, the Chinese University of Hong kong, Shatin, Hong Kong
%            mailto:thierry.blu@m4x.org

dy=diff(diff(y,2,1),2,2);
est_noise_variance=(median(abs(dy(:)))/(6*sqrt(2)*erfcinv(0.5)))^2;
